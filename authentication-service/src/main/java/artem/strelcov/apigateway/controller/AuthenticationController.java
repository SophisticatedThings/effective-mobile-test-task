package artem.strelcov.apigateway.controller;

import artem.strelcov.apigateway.dto.AuthenticationRequest;
import artem.strelcov.apigateway.dto.AuthenticationResponse;
import artem.strelcov.apigateway.dto.RegisterRequest;
import artem.strelcov.apigateway.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
@Validated
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
             @Valid @RequestBody RegisterRequest request
    ) {
        authenticationService.register(request);
        return new ResponseEntity<String>(
                "Вы успешно зарегестрировались", HttpStatus.CREATED
        );
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

}