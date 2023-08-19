package artem.strelcov.postsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "images")
public class ImageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;
    @Column(name = "image_url")
    private String imageUrl;

    /*@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "posts_images",
            joinColumns = {
                    @JoinColumn(name = "images_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "posts_id")
            }
    )
    private Post post; */

}
