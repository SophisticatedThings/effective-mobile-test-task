package artem.strelcov.postsservice.service;

import artem.strelcov.postsservice.dto.PostDto;
import artem.strelcov.postsservice.model.ImageModel;
import artem.strelcov.postsservice.model.Post;
import artem.strelcov.postsservice.repository.PostRepository;
import artem.strelcov.postsservice.util.MapPostToPostDto;
import artem.strelcov.postsservice.util.MapToStringUrls;
import artem.strelcov.postsservice.util.PostSort;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MinioClient minioClient;
    private final WebClient.Builder webClient;

    public List<Post> getAllPostsExceptMy(Principal user) {
        return postRepository.getAllPostsWhereUsernameNotInRequest(user.getName());
    }
    public List<PostDto> getPostsByUsername(String username) {
        return postRepository.getPostsByUsername(username).stream()
                .map(post ->
                        PostDto.builder()
                                .title(post.getTitle())
                                .username(post.getUsername())
                                .content(post.getContent())
                                .createdAt(post.getCreatedAt())
                                .postImages(MapToStringUrls.map(post.getPostImages()))
                                .build()
                )
                .toList();


    }

    public Post updatePost(Integer id,Post post,MultipartFile [] images,
                           Principal user) {
        if (!Objects.equals(getPostById(id).getUsername(), user.getName())) {
            throw  new RuntimeException("you can't update this post");
        }
        Post postToUpdate = getPostById(id);
        postToUpdate.setTitle(post.getTitle());
        postToUpdate.setContent(post.getContent());
        addImagesToMinioAndPost(images,postToUpdate);
        return postRepository.save(postToUpdate);
    }
    public void deletePost(Integer id) {
        postRepository.deleteById(id);
    }
    private Post getPostById(Integer id) {
        return postRepository.getById(id);
    }
    public Post createPost(MultipartFile [] images, Post post, Principal user) {

        post.setPostImages(new ArrayList<>(images.length));
        addImagesToMinioAndPost(images, post);
        fillPostWithUsername(post, user);
        post.setCreatedAt(LocalDateTime.now());
        postRepository.save(post);
        return post;

    }
    private void addImagesToMinioAndPost(MultipartFile [] images, Post post) {
        for(MultipartFile image : images) {
            String imageName = generateImageName(image);

            try (InputStream inputStream = new BufferedInputStream(image.getInputStream())) {
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
                .substring(image.getOriginalFilename().lastIndexOf(".") + 1);
    }
    private void fillPostWithImageUrl(String imageName, Post post) {
        ImageModel image = ImageModel.builder()
                .imageUrl(imageName)
                .build();
        post.getPostImages().add(image);
    }
    private void fillPostWithUsername(Post post, Principal user) {
        post.setUsername(user.getName());
    }

    public Page<PostDto> getPostsBySubscriptionsWithPagination(HttpServletRequest request,Principal user, Integer offset, Integer limit, PostSort sort) {
        List<String> subscriptions = getSubscriptionsOfUser(request, user.getName());
        if(sort != null) {
            Page<Post> posts = postRepository.findAllByUsernameIn(
                    subscriptions,
                    PageRequest.of(offset,limit,sort.getSortValue()));

            return posts.map(new MapPostToPostDto());
        }
        return postRepository.findAllByUsernameIn(
                subscriptions,
                PageRequest.of(offset,limit))
                        .map(new MapPostToPostDto());
    }

    private List<String> getSubscriptionsOfUser(HttpServletRequest request, String username) {
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
