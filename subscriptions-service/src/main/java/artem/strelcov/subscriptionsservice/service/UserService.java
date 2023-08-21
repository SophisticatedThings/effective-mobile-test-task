package artem.strelcov.subscriptionsservice.service;

import artem.strelcov.subscriptionsservice.model.Subscription;
import artem.strelcov.subscriptionsservice.model.User;
import artem.strelcov.subscriptionsservice.repository.SubscriptionRepository;
import artem.strelcov.subscriptionsservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.security.Principal;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

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
    public Set<User> getSubscriptions(Principal user) {
        User requestInitiator = getUserByUsername(user.getName());
        return requestInitiator.getSubscriptions();
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
}
