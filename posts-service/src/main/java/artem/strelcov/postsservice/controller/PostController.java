package artem.strelcov.postsservice.controller;

import artem.strelcov.postsservice.dto.PostDto;
import artem.strelcov.postsservice.dto.PostRequest;
import artem.strelcov.postsservice.model.Post;
import artem.strelcov.postsservice.service.PostService;
import artem.strelcov.postsservice.util.PostSort;
import artem.strelcov.postsservice.util.RestResponsePage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Posts-service")
public class PostController {

    private final PostService postServiceImpl;
    @Operation(
            summary = "Создает пост",
            description = "Метод создает пост на основании postRequest. Пост содержит" +
                    " название, текст, и фотографии. Последние хранятся на сервере Minio"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> createPost(
            PostRequest postRequest,
            @Parameter(
                    description = "Images to be uploaded",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam(required = false,value = "images") List<MultipartFile> images,
            Principal user){

        Post createdPost = postServiceImpl.createPost(images,postRequest, user);
        return new ResponseEntity<Post>(createdPost, HttpStatus.CREATED);
    }
    @Operation(
            summary = "Получение всех постов кроме человека, который делает запрос"
    )
    @GetMapping
    public ResponseEntity<List<Post>> getAllPostsExceptMy(Principal user) {

        return new ResponseEntity<List<Post>>(
                postServiceImpl.getAllPostsExceptMy(user),
                HttpStatus.OK);
    }
    @Operation(
            summary = "Получение всех постов кроме человека, который делает запрос, и пагинация",
            description = "Метод позволяет получить последние посты от пользователей," +
                    " на которых подписан человек, отправляющий запрос. Массив ответа" +
                    "делится на страницы, как именно - решать Вам. limit задает ограничение" +
                    "на количество постов на одной странице. offset - задает номер страницы." +
                    "То есть, если, скажем, у Вас в ответе 5 постов, то можно задать limit=1" +
                    ",offset = 3, и тогда Вам выведется 3й из 5 постов. Кроме того, есть параметр" +
                    "sort, который задает тип сортировки результатов по времени создания поста"
    )
    @GetMapping("/pagination")
    public RestResponsePage<PostDto> getPostsBySubscriptionsWithPagination(
            HttpServletRequest request,
            Principal user,
            @RequestParam("offset") Integer offset,
            @RequestParam("limit") Integer limit,
            @RequestParam(required = false, value = "sort") PostSort sort
    ) {

        return postServiceImpl.getPostsBySubscriptionsWithPagination(
                request,user,offset,limit, sort);
    }
    @Operation(
            summary = "Получение всех постов пользователя с заданным username"
    )
    @GetMapping("/{username}")
    public ResponseEntity<List<PostDto>> getPostsByUsername(
            @PathVariable("username") String username) {
        return new ResponseEntity<List<PostDto>>(
                postServiceImpl.getPostsByUsername(username),
                HttpStatus.OK);
    }
    @Operation(
            summary = "Изменение данных поста. Можно поменять title, content, и добавить" +
                    " фотографии."
    )
    @PutMapping(value = "/{postId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> updatePost(
            @PathVariable Integer postId,
            PostRequest postRequest,
            @Parameter(
                    description = "Images to be uploaded",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam(required = false,value = "images") List<MultipartFile> images,
            Principal user
    ) {
        return new ResponseEntity<Post>(
                postServiceImpl.updatePost(postId,postRequest,images, user),
                HttpStatus.OK);
    }
    @Operation(
            summary = "Удаление поста с айди id"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(
            @PathVariable("id") Integer id, Principal user) {

        postServiceImpl.deletePost(id, user);
        return new ResponseEntity<String>("Пост успешно удален", HttpStatus.OK);
    }

}
