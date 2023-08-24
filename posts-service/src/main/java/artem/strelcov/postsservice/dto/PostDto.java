package artem.strelcov.postsservice.dto;

import artem.strelcov.postsservice.model.ImageModel;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostDto {
    private Integer id;
    private String title;
    private String content;
    private String username;
    private LocalDateTime createdAt;
    private List<String> postImages;

}
