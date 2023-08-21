package artem.strelcov.apigateway.controller;

import artem.strelcov.apigateway.dto.AuthenticationRequest;
import artem.strelcov.apigateway.dto.AuthenticationResponse;
import artem.strelcov.apigateway.dto.RegisterRequest;
import artem.strelcov.apigateway.dto.UserDTO;
import artem.strelcov.apigateway.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }

    /*@PostMapping("/testing")
    public void authenticate(@RequestBody UserDTO userDTO)  {
        service.testing(userDTO);
    } */

}