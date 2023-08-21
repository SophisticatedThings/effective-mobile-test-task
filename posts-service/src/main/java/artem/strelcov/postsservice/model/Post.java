package artem.strelcov.postsservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;
    @Column(name = "title")
    private String title;
    @Column(name = "content")
    private String content;
    @Column(name = "username")
    private String username;
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
