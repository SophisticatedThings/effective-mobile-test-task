package artem.strelcov.subscriptionsservice.service;

import artem.strelcov.subscriptionsservice.dto.PostDto;
import artem.strelcov.subscriptionsservice.model.User;
import artem.strelcov.subscriptionsservice.util.PostSort;
import artem.strelcov.subscriptionsservice.util.RestResponsePage;
import artem.strelcov.subscriptionsservice.util.SortType;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.List;
import java.util.Set;

public interface UserService {
    public void replicateUser(String username);
    public List<User> getAllUsers();
    public void subscribe(Principal user, Integer authorId);
    public void unsubscribe(Principal user, Integer authorId);
    public List<String> getSubscriptions(String username);
    public void sendFriendshipRequest(Principal user, Integer authorId);
    public Set<User> getFriendshipRequests(Principal user);
    public Set<User> getFriends(Principal user);
    public boolean acceptOrDeclineRequest(
            Principal user, Boolean acceptRequest, Integer senderId);
    public void deleteFromFriends(Principal user, Integer friendId);
    public void createChat(Principal user, Integer companionId);
    public List<PostDto> getRecentPosts(
            Principal user, SortType sortType, HttpServletRequest request);
    public RestResponsePage<PostDto> getRecentPostsWithPagination(
            HttpServletRequest request,
            Integer offset, Integer limit, PostSort sort);
}
