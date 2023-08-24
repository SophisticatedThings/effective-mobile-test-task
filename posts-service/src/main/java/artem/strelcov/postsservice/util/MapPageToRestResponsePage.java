package artem.strelcov.postsservice.util;


import artem.strelcov.postsservice.dto.PostDto;
import org.springframework.data.domain.Page;

public class MapPageToRestResponsePage {
    public static RestResponsePage<PostDto> map(Page<PostDto> pageDto) {

        return new RestResponsePage<PostDto>(
                pageDto.getContent(),
                pageDto.getNumber(),
                pageDto.getSize(),
                pageDto.getTotalElements()
        );
    }
}
