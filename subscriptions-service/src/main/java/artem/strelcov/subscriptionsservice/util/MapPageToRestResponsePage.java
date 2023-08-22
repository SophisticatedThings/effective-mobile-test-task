package artem.strelcov.subscriptionsservice.util;


import artem.strelcov.subscriptionsservice.dto.PostDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;

public class MapPageToRestResponsePage {
    public static RestResponsePage<PostDto> map(Page<PostDto> pageDto) {

        return new RestResponsePage<PostDto>(
                pageDto.getContent(),
                pageDto.getNumber(),
                pageDto.getSize(),
                pageDto.getTotalElements(),
                (JsonNode) pageDto.getPageable(),
                pageDto.isLast(),
                pageDto.getTotalPages(),
                pageDto.isFirst(),
                pageDto.getNumberOfElements()
        );
    }
}
