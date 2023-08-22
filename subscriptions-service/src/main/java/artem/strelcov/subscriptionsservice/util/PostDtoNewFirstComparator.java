package artem.strelcov.subscriptionsservice.util;

import artem.strelcov.subscriptionsservice.dto.PostDto;

import java.util.Comparator;

public class PostDtoNewFirstComparator implements Comparator<PostDto> {
    @Override
    public int compare(PostDto post, PostDto post2) {
        return post2.getCreatedAt().compareTo(post.getCreatedAt());
    }
}
