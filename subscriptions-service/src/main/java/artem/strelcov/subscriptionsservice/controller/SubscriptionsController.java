package artem.strelcov.subscriptionsservice.controller;

import artem.strelcov.subscriptionsservice.dto.PostDto;
import artem.strelcov.subscriptionsservice.dto.UserDTO;
import artem.strelcov.subscriptionsservice.model.User;
import artem.strelcov.subscriptionsservice.service.UserServiceImp;
import artem.strelcov.subscriptionsservice.util.PostSort;
import artem.strelcov.subscriptionsservice.util.RestResponsePage;
import artem.strelcov.subscriptionsservice.util.SortType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Subscriptions-service")
public class SubscriptionsController {

    private final UserServiceImp userServiceImp;
    @Operation(
            summary = "Метод используется сервисом auth, Вам его трогать не нужно"
    )
    @PostMapping("/replicate")
    public void replicateUser(@RequestBody UserDTO userDTO) {
        userServiceImp.replicateUser(userDTO.getUsername());

    }
    @Operation(
            summary = "Метод возвращает всех пользователей в приложении"
    )
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<List<User>>(userServiceImp.getAllUsers(), HttpStatus.OK);
    }
    @Operation(
            summary = "Метод позволяет запросщику подписаться на пользователя с authorId"
    )
    @PutMapping("/subscribe/{authorId}")
    public ResponseEntity<String> subscribe(
            Principal user,
            @PathVariable Integer authorId) {
        userServiceImp.subscribe(user, authorId);
        return new ResponseEntity<String>("вы успешно подписались", HttpStatus.OK);
    }
    @Operation(
            summary = "Метод позволяет отписаться от пользователя с authorId "
    )
    @PutMapping("/unsubscribe/{author_id}")
    public ResponseEntity<String> unsubscribe(
            Principal user,
            @PathVariable("author_id") Integer authorId) {
        userServiceImp.unsubscribe(user, authorId);
        return new ResponseEntity<String>("Вы успешно отписались", HttpStatus.OK);
    }
    @Operation(
            summary = "Метод возвращает юзернеймы всех пользователей, на которых" +
                    " подписан запросщик. В качестве параметра username запросщика "
    )
    @GetMapping("/{username}")
    public List<String> getSubscriptions(@PathVariable("username") String username) {
        return userServiceImp.getSubscriptions(username);
    }
    @Operation(
            summary = "Запросщик отправляет запрос в друзья юзеру с author_id"
    )
    @PutMapping("/send-friendship-request/{author_id}")
    public ResponseEntity<String> sendFriendshipRequest(
            Principal user,
            @PathVariable("author_id") Integer authorId) {
        userServiceImp.sendFriendshipRequest(user,authorId);
        return new ResponseEntity<String>("Вы отправили заявку в друзья", HttpStatus.OK);

    }
    @Operation(
            summary = "Метод позволяет запросщику посмотреть, какие юзеры отправили заявки"
    )
    @GetMapping("/friendship-requests")
    public ResponseEntity<Set<User>> getFriendshipRequests(Principal user) {
        return new ResponseEntity<Set<User>>(
                userServiceImp.getFriendshipRequests(user), HttpStatus.OK);
    }
    @Operation(
            summary = "Возвращает список друзей запросщика"
    )
    @GetMapping("/friends")
    public ResponseEntity<Set<User>> getFriends(Principal user) {
        return new ResponseEntity<Set<User>>(
                userServiceImp.getFriends(user), HttpStatus.OK);
    }
    @Operation(
            summary = "Метод позволяет принять/отклонить заяку в друзья от пользователя" +
                    " с айди sender_id"
    )
    @PutMapping("/friendship-requests/{sender_id}")
    public ResponseEntity<Boolean> acceptOrDeclineRequest(
            Principal user,
            @RequestParam("acceptRequest") Boolean acceptRequest,
            @PathVariable("sender_id") Integer senderId) {
        return new ResponseEntity<Boolean>(userServiceImp.acceptOrDeclineRequest(
                user,acceptRequest,senderId), HttpStatus.OK);
    }
    @Operation(
            summary = "Метод удаляет из друзей запросщика пользователя с айди friend_id"
    )
    @DeleteMapping("/friendships/{friend_id}")
    public ResponseEntity<String> deleteFromFriends(
            Principal user,
            @PathVariable("friend_id") Integer friendId) {
        userServiceImp.deleteFromFriends(user,friendId);
        return new ResponseEntity<String>(
                "Пользователь успешно удален из списка друзей", HttpStatus.OK);
    }
    @Operation(
            summary = "Метод иммитирует создание чата запросщика с пользователем" +
                    " companion_id"
    )
    @GetMapping("/chat/{companion_id}")
    public ResponseEntity<String> getChat(
            Principal user,
            @PathVariable("companion_id") Integer companionId) {
        userServiceImp.createChat(user,companionId);
        return new ResponseEntity<String>("Чат успешно создан", HttpStatus.OK);
    }
    @Operation(
            summary = "Метод отображает все последние посты от пользователей, на " +
                    "которых подписан запросщик. Можно так же задать сортировку по" +
                    " времени создания"
    )
    @GetMapping("/recent_posts")
    public ResponseEntity<List<PostDto>> getRecentPosts(
            Principal user,
            @RequestParam(required = false, value = "sortType") SortType sortType,
            HttpServletRequest request) {
        return new ResponseEntity<List<PostDto>>(
                userServiceImp.getRecentPosts(user,sortType,request), HttpStatus.OK);
    }
    @Operation(
            summary = "Получение всех последних постов по подписке и пагинация",
            description = "Метод позволяет получить последние посты от пользователей," +
                    " на которых подписан человек, отправляющий запрос. Массив ответа" +
                    "делится на страницы, как именно - решать Вам. limit задает ограничение" +
                    "на количество постов на одной странице. offset - задает номер страницы." +
                    "То есть, если, скажем, у Вас в ответе 5 постов, то можно задать limit=1" +
                    ",offset = 3, и тогда Вам выведется 3й из 5 постов. Кроме того, есть параметр" +
                    "sort, который задает тип сортировки результатов по времени создания поста"
    )
    @GetMapping("/recent_posts/pagination")
    public RestResponsePage<PostDto> getRecentPostsWithPagination(
            HttpServletRequest request,
            @RequestParam("offset") Integer offset,
            @RequestParam("limit") Integer limit,
            @RequestParam(required = false, value = "sort") PostSort sort
    ) {
        return userServiceImp.getRecentPostsWithPagination(
                request,offset,limit,sort);
    }

}
