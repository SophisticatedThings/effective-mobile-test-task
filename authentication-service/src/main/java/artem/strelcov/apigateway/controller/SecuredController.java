package artem.strelcov.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/secured")
public class SecuredController {

    @GetMapping
    public ResponseEntity<String> securedMethod() {
        return ResponseEntity.ok("Secured method");
    }
}
