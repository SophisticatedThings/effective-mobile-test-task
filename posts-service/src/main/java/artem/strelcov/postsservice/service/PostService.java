package artem.strelcov.postsservice.service;

import artem.strelcov.postsservice.model.ImageModel;
import artem.strelcov.postsservice.model.Post;
import artem.strelcov.postsservice.repository.PostRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MinioClient minioClient;

    public List<Post> getAllPostsExceptMy(Principal user) {
        return postRepository.getAllPostsWhereUsernameNotInRequest(user.getName());
    }
    public List<Post> getPostsByUsername(String username) {
        return postRepository.getPostsByUsername(username);
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

}
