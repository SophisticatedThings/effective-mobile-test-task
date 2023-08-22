package artem.strelcov.subscriptionsservice.util;

import artem.strelcov.subscriptionsservice.dto.PostDto;

import java.util.Comparator;

public class PostDtoOldFirstComparator implements Comparator<PostDto> {

    @Override
    public int compare(PostDto post, PostDto post2) {
        return post.getCreatedAt().compareTo(post2.getCreatedAt());
    }
}
