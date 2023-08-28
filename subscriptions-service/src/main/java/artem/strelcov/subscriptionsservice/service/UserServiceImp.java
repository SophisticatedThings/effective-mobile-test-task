package artem.strelcov.subscriptionsservice.service;

import artem.strelcov.subscriptionsservice.dto.PostDto;
import artem.strelcov.subscriptionsservice.exception_handling.NoPostsException;
import artem.strelcov.subscriptionsservice.exception_handling.NotFriendsException;
import artem.strelcov.subscriptionsservice.model.User;
import artem.strelcov.subscriptionsservice.repository.UserRepository;
import artem.strelcov.subscriptionsservice.util.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImp implements UserService{

    private final UserRepository userRepository;
    private final WebClient.Builder webClient;

    public void replicateUser(String username) {

        var user = User.builder()
                .username(username)
                .build();
        userRepository.save(user);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public void subscribe(Principal user, Integer authorId) {
        User requestInitiator = getUserByUsername(user.getName());
        User author = getUserById(authorId).orElseThrow(() ->
                new NoSuchElementException(
                "Пользователя с таким id не существует"));
        author.getSubscribers().add(requestInitiator);
        userRepository.save(author);
    }
    public void unsubscribe(Principal user, Integer authorId) {
        User requestInitiator = getUserByUsername(user.getName());
        User author = getUserById(authorId).orElseThrow(() ->
                new NoSuchElementException(
                        "Пользователя с таким id не существует"));
        author.getSubscribers().remove(requestInitiator);
        userRepository.save(author);
    }
    public List<String> getSubscriptions(String username) {
        User requestInitiator = getUserByUsername(username);
        return requestInitiator.getSubscriptions().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }
    public void sendFriendshipRequest(Principal user, Integer authorId) {
        subscribe(user, authorId);
        User requestInitiator = getUserByUsername(user.getName());
        User author = getUserById(authorId).orElseThrow(() ->
                new NoSuchElementException(
                        "Пользователя с таким id не существует"));
        author.getFriendshipRequests().add(requestInitiator);
        userRepository.save(author);
    }
    public Set<User> getFriendshipRequests(Principal user) {
        User requestInitiator = getUserByUsername(user.getName());
        return requestInitiator.getFriendshipRequests();
    }
    public boolean acceptOrDeclineRequest(
            Principal user, Boolean acceptRequest, Integer senderId) {
        User requestInitiator = getUserByUsername(user.getName());
        User friendshipRequestSender = getUserById(senderId).orElseThrow(() ->
                new NoSuchElementException(
                        "Пользователя с таким id не существует"));
        if(!requestInitiator.getFriendshipRequests().contains(friendshipRequestSender))
             throw new NotFriendsException("Пользователь с senderId не отправлял Вам заявку");
        if(!acceptRequest){
            requestInitiator.getFriendshipRequests().remove(friendshipRequestSender);
            userRepository.save(requestInitiator);
            return false;
        }
        friendshipRequestSender.getSubscribers().add(requestInitiator);
        userRepository.save(friendshipRequestSender);
        requestInitiator.getFriends().add(friendshipRequestSender);
        requestInitiator.getFriendshipRequests().remove(friendshipRequestSender);
        userRepository.save(requestInitiator);
        return true;
    }
    public void deleteFromFriends(Principal user, Integer friendId) {
        User requestInitiator = getUserByUsername(user.getName());
        User friend = getUserById(friendId).orElseThrow(() ->
                new NoSuchElementException(
                        "Пользователя с таким id не существует"));
        requestInitiator.getFriends().remove(friend);
        friend.getSubscribers().remove(requestInitiator);
        userRepository.save(requestInitiator);
        userRepository.save(friend);
    }
    public void createChat(Principal user, Integer companionId) {
        User requestInitiator = getUserByUsername(user.getName());
        User companion = getUserById(companionId).orElseThrow(() ->
                new NoSuchElementException(
                        "Пользователя с таким id не существует"));
        boolean isContainsCompanion =
                requestInitiator.getFriends().contains(companion);
        boolean isContainsInitiator =
                companion.getFriends().contains(requestInitiator);
        if(isContainsCompanion && isContainsInitiator) {

        }
        else{
            throw new NotFriendsException("Вы не являетесь другом пользователя");
        }
    }
    public List<PostDto> getRecentPosts(
            Principal user, SortType sortType, HttpServletRequest request) {

        User requestInitiator = getUserByUsername(user.getName());
        Set<User> subscriptions = requestInitiator.getSubscriptions();
        List<PostDto> result = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        String jwtToken = request.getHeader("Authorization").substring(7);
        if(subscriptions.isEmpty())
            throw new NoPostsException("Вы ни на кого не подписаны");
        for(User author : subscriptions) {
                try {
                    PostDto[] posts = webClient.build().get()
                            .uri("http://posts-service:8090/api/posts/{username}",
                                    uriBuilder -> uriBuilder
                                            .build(author.getUsername()))
                            .headers(httpHeaders -> httpHeaders.setBearerAuth(jwtToken))
                            .retrieve()
                            .bodyToMono(PostDto[].class)
                            .block();
                    for (PostDto post : posts) {
                        if (ChronoUnit.HOURS.between(post.getCreatedAt(), currentTime) < 24) {
                            result.add(post);
                        }
                    }
                }
                catch (RuntimeException e){
                    continue;
                }
        }
        if(sortType != null && sortType.equals(SortType.DATE_ASC)) {
            return result.stream()
                    .sorted(new PostDtoOldFirstComparator())
                    .collect(Collectors.toList());
        }
        else if(sortType != null && sortType.equals(SortType.DATE_DESC)) {
            return result.stream()
                    .sorted(new PostDtoNewFirstComparator())
                    .collect(Collectors.toList());
        }
        return result;
    }
    public RestResponsePage<PostDto> getRecentPostsWithPagination(
             HttpServletRequest request,
             Integer offset, Integer limit, PostSort sort) {

        String jwtToken = request.getHeader("Authorization").substring(7);
        return webClient.build()
                .get()
                .uri("http://posts-service:8090/api/posts/pagination",
                        uriBuilder -> uriBuilder
                                .queryParam("offset", offset)
                                .queryParam("limit", limit)
                                .queryParam("sort", sort)
                                .build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwtToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RestResponsePage<PostDto>>() {
                })
                .block();
    }
    private User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }
    private Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Set<User> getFriends(Principal user) {
        User requestInitiator = getUserByUsername(user.getName());
        return requestInitiator.getFriends();
    }
}
