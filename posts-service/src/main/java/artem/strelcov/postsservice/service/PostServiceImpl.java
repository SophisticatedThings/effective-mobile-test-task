package artem.strelcov.postsservice.service;

import artem.strelcov.postsservice.dto.PostDto;
import artem.strelcov.postsservice.exception_handling.validation.GetPostsException;
import artem.strelcov.postsservice.exception_handling.validation.UpdatePostException;
import artem.strelcov.postsservice.model.ImageModel;
import artem.strelcov.postsservice.model.Post;
import artem.strelcov.postsservice.repository.PostRepository;
import artem.strelcov.postsservice.util.*;
import io.minio.*;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static artem.strelcov.postsservice.util.PostSort.DATE_ASC;
import static artem.strelcov.postsservice.util.PostSort.DATE_DESC;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final MinioClient minioClient;
    private final WebClient.Builder webClient;

    public Post createPost(MultipartFile [] images,
                           Post post, Principal user) {

        post.setPostImages(new ArrayList<>());
        if(images != null)
            addImagesToMinioAndPost(images, post);
        post.setUsername(user.getName());
        post.setCreatedAt(LocalDateTime.now());
        postRepository.save(post);
        return post;

    }
    public List<Post> getAllPostsExceptMy(Principal user) {

          return postRepository.getAllPostsWhereUsernameNotInRequest(user.getName());

    }
    /**
     * Возвращает все посты, которые создал пользователь с ником username
     */
    public List<PostDto> getPostsByUsername(String username) {
        List<Post> posts = postRepository.getPostsByUsername(username);
        if(posts.isEmpty()) {
            throw new GetPostsException("У данного пользователя нет постов, либо" +
                    "введен некорректный username");
        }
        return posts.stream()
                .map(post ->
                        PostDto.builder()
                                .id(post.getId())
                                .title(post.getTitle())
                                .username(post.getUsername())
                                .content(post.getContent())
                                .createdAt(post.getCreatedAt())
                                .postImages(MapToStringUrls.map(post.getPostImages()))
                                .build()
                )
                .toList();
    }
    /**
     * Метод возвращает объект Page, который содержит в себе список постов каждого
     * пользователя, на которого подписан Principal, вызвавший данный метод
     * @param request - требуется для получения токена запроса, который используется
     *                в приватном вспомогательном методе getSubscriptionsOfUser
     * @param user - Principal, который вызвал метод и для которого нужно получить
     *                последние посты от тех, на кого он подписан
     * @param offset - номер страницы, которую нужно вывести
     * @param limit - количество постов, которые можно вывести на одной странице
     * @param sort - тип сортировки постов по времени создания. Принимает одно из следующих
     *                значений: DATE_ASC - показать сначала старые посты
     *                          DATE_DESC - показать сначала новые посты
     *                          отсутствие этого request_param - нет сортировки
     * @return - возвращает обертку над Page, и представляет из себя список постов и
     *              параметры пагинации, которые можно будет обрабатывать на фронте
     */
    public RestResponsePage<PostDto> getPostsBySubscriptionsWithPagination(
            HttpServletRequest request, Principal user,
            Integer offset, Integer limit, PostSort sort) {

        List<String> subscriptions = getSubscriptionsOfUser(request, user.getName());
        Page<Post> posts = null;
        if(sort != null && (sort.equals(DATE_ASC) || sort.equals(DATE_DESC))) {
           posts = postRepository.findAllByUsernameIn(
                    subscriptions,
                    PageRequest.of(offset,limit,sort.getSortValue()));

            return MapPageToRestResponsePage.map(posts.map(new MapPostToPostDto()));
        }
        posts = postRepository.findAllByUsernameIn(
                subscriptions,
                PageRequest.of(offset,limit));
        return MapPageToRestResponsePage.map(posts.map(new MapPostToPostDto()));
    }
    /**
     * Метод позволяет изменить title, content конкретного поста с параметром id.
     * Кроме того, метод позволяет вместе с этим добавить новые изображения в пост.
     */
    public Post updatePost(Integer id,Post post,MultipartFile [] images,
                           Principal user) {

        Post postToUpdate = validatePostIdAndOwner(id,user);
        postToUpdate.setTitle(post.getTitle());
        postToUpdate.setContent(post.getContent());
        if(images != null)
            addImagesToMinioAndPost(images,postToUpdate);

        return postRepository.save(postToUpdate);
    }
    public void deletePost(Integer id, Principal user) {
        validatePostIdAndOwner(id,user);
        postRepository.deleteById(id);
    }

    /**
     * Метод проверяет, существует ли пост с id, и принадлежит ли он пользователю user
     */
    private Post validatePostIdAndOwner(Integer id, Principal user){
        Optional<Post> postToUpdateOptional = getPostById(id);
        if(postToUpdateOptional.isEmpty()) {
            throw new UpdatePostException("Указанный Вами пост не существует." +
                    "Проверьте,пожалуйста, id");
        }
        Post postToUpdate = postToUpdateOptional.get();

        if (!Objects.equals(postToUpdate.getUsername(), user.getName())) {
            throw new UpdatePostException("Вы не можете обновить пост, " +
                    "поскольку не являетесь его автором");
        }
        return postToUpdate;

    }
    private Optional<Post> getPostById(Integer id) {
        return postRepository.findById(id);
    }

    /**
     * Метод загружает на сервер Minio фотографии для поста post(с шифрованием имени файла)
     * и добавляет информацию о добавленных фотографиях в post
     */
    private void addImagesToMinioAndPost(MultipartFile [] images, Post post) {
        for(MultipartFile image : images) {
            String imageName = generateImageName(image);

            try (InputStream inputStream =
                         new BufferedInputStream(image.getInputStream())) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket("images")
                                .object(imageName)
                                .stream(inputStream, -1, 10485760)
                                .contentType("application/octet-stream")
                                .build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ServerException e) {
                throw new RuntimeException(e);
            } catch (InsufficientDataException e) {
                throw new RuntimeException(e);
            } catch (ErrorResponseException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (InvalidResponseException e) {
                throw new RuntimeException(e);
            } catch (XmlParserException e) {
                throw new RuntimeException(e);
            } catch (InternalException e) {
                throw new RuntimeException(e);
            }

            fillPostWithImageUrl(imageName, post);
        }
    }
    private String generateImageName(MultipartFile image) {
        String extension = getExtension(image);
        return UUID.randomUUID() + "." + extension;
    }
    private String getExtension(MultipartFile image) {
        return image.getOriginalFilename()
                .substring(image.getOriginalFilename()
                        .lastIndexOf(".") + 1);
    }
    private void fillPostWithImageUrl(String imageName, Post post) {
        ImageModel image = ImageModel.builder()
                .imageUrl(imageName)
                .build();
        post.getPostImages().add(image);
    }

    /**
     * Синхронный межсервисный запрос, который возвращает список подписок пользователя username
     */
    private List<String> getSubscriptionsOfUser(
            HttpServletRequest request, String username) {

        String jwtToken = request.getHeader("Authorization").substring(7);
        return webClient.build().get()
                .uri("http://localhost:8085/api/subscriptions/{username}",
                        uriBuilder -> uriBuilder
                                .build(username))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwtToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block();
    }
}
