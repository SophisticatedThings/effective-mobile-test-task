package artem.strelcov.postsservice.util;

import artem.strelcov.postsservice.dto.PostDto;
import artem.strelcov.postsservice.model.Post;

import java.util.function.Function;

public class MapPostToPostDto implements Function<Post, PostDto> {

    @Override
    public PostDto apply(Post post) {
        return PostDto.builder()
                .title(post.getTitle())
                .username(post.getUsername())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .postImages(MapToStringUrls.map(post.getPostImages()))
                .build();
    }
}
