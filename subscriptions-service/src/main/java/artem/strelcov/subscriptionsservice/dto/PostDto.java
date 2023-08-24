package artem.strelcov.subscriptionsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {

    private String title;
    private String content;
    private String username;
    private LocalDateTime createdAt;
    private List<String> postImages;

}
