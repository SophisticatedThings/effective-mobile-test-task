package artem.strelcov.subscriptionsservice.service;

import artem.strelcov.subscriptionsservice.dto.PostDto;
import artem.strelcov.subscriptionsservice.model.User;
import artem.strelcov.subscriptionsservice.repository.UserRepository;
import artem.strelcov.subscriptionsservice.util.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
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
public class UserService {

    private final UserRepository userRepository;
    private final WebClient.Builder webClient;

    public void replicateUser(String username) {

        var user = User.builder()
                .username(username)
                .build();
        userRepository.save(user);
    }

    public void subscribe(Principal user, Integer authorId) {
        User requestInitiator = getUserByUsername(user.getName());
        User author = getUserById(authorId);
        author.getSubscribers().add(requestInitiator);
        userRepository.save(author);


    }
    public User unsubscribe(Principal user, Integer authorId) {
        User requestInitiator = getUserByUsername(user.getName());
        User author = getUserById(authorId);
        author.getSubscribers().remove(requestInitiator);

        return userRepository.save(author);
    }
    public List<String> getSubscriptions(String username) {
        User requestInitiator = getUserByUsername(username);
        return requestInitiator.getSubscriptions().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }
    private User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }
    private User getUserById(Integer id) {
        return userRepository.findById(id).get();

    }

    public User sendFriendshipRequest(Principal user, Integer authorId) {
        subscribe(user, authorId);
        User requestInitiator = getUserByUsername(user.getName());
        User author = getUserById(authorId);
        author.getFriendshipRequests().add(requestInitiator);
        return userRepository.save(author);
    }

    public Set<User> getFriendshipRequests(Principal user) {
        User requestInitiator = getUserByUsername(user.getName());
        return requestInitiator.getFriendshipRequests();
    }

    public boolean acceptOrDeclineRequest(Principal user, Boolean acceptRequest, Integer senderId) {
        User requestInitiator = getUserByUsername(user.getName());
        User friendshipRequestSender = getUserById(senderId);
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteFromFriends(Principal user, Integer friendId) {
        User requestInitiator = getUserByUsername(user.getName());
        User friend = getUserById(friendId);
        requestInitiator.getFriends().remove(getUserById(friendId));
        friend.getSubscribers().remove(requestInitiator);
        userRepository.save(requestInitiator);
        userRepository.save(friend);
    }

    public void createChat(Principal user, Integer companionId) {
        User requestInitiator = getUserByUsername(user.getName());
        User companion = getUserById(companionId);
        boolean isContainsCompanion = requestInitiator.getFriends().contains(companion);
        boolean isContainsInitiator = companion.getFriends().contains(requestInitiator);
        if(!(isContainsCompanion && isContainsInitiator)) {
            throw new RuntimeException();
        }
        return;
    }

    public List<PostDto> getRecentPosts(Principal user, String sortType, HttpServletRequest request) {

        User requestInitiator = getUserByUsername(user.getName());
        Set<User> subscriptions = requestInitiator.getSubscriptions();
        List<PostDto> result = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        String jwtToken = request.getHeader("Authorization").substring(7);
        for(User author : subscriptions) {
            PostDto [] posts = webClient.build().get()
                    .uri("http://localhost:8090/api/posts/{username}",
                            uriBuilder -> uriBuilder
                                    .build(author.getUsername()))
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(jwtToken))
                    .retrieve()
                    .bodyToMono(PostDto[].class)
                    .block();
            for(PostDto post : posts) {
                if (ChronoUnit.HOURS.between(post.getCreatedAt(), currentTime) < 24) {
                    result.add(post);
                }
            }
        }
        if(sortType != null && sortType.equals("oldFirst")) {
            return result.stream()
                    .sorted(new PostDtoOldFirstComparator())
                    .collect(Collectors.toList());
        }
        else if(sortType != null && sortType.equals("newFirst")) {
            return result.stream()
                    .sorted(new PostDtoNewFirstComparator())
                    .collect(Collectors.toList());
        }
        return result;
    }

    public RestResponsePage<PostDto> getRecentPostsWithPagination(
            Principal user, HttpServletRequest request,
             Integer offset, Integer limit, PostSort sort) {
        User requestInitiator = getUserByUsername(user.getName());
        String jwtToken = request.getHeader("Authorization").substring(7);
        Page<PostDto> postsDtos =  webClient.build()
                    .get()
                    .uri("http://localhost:8090/api/posts/pagination",
                            uriBuilder -> uriBuilder
                                    .queryParam("offset", offset)
                                    .queryParam("limit", limit)
                                    .queryParam("sort", sort)
                                    .build())
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(jwtToken))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Page<PostDto>>() {})
                    .block();
        return MapPageToRestResponsePage.map(postsDtos);
}

    private List<String> mapUserToUsername(Set<User> subscriptions) {
        return subscriptions.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }
}
