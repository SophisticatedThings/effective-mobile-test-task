package artem.strelcov.postsservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;
    @NotBlank(message = "Требуется название поста")
    @Column(name = "title")
    private String title;
    @NotBlank(message = "Требуется ввести контент")
    @Column(name = "content")
    private String content;
    @Column(name = "username")
    private String username;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @OneToMany( cascade = CascadeType.ALL)
    @JoinTable(name = "posts_images",
            joinColumns = {
                @JoinColumn(name = "posts_id")
            },
            inverseJoinColumns = {
                @JoinColumn(name = "images_id")
            }
    )
    private List<ImageModel> postImages;

}
