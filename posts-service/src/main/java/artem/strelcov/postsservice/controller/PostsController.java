package artem.strelcov.postsservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostsController {
    @GetMapping
    public ResponseEntity<String> posts() {
        return ResponseEntity.ok("Secured posts");
    }

}
