package artem.strelcov.postsservice.repository;

import artem.strelcov.postsservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("select p from Post p where p.username not in (:username)")
    List<Post> getAllPostsWhereUsernameNotInRequest(String username);
    @Query("select p from Post p where p.username=:username")
    List<Post> getPostsByUsername(String username);
}
