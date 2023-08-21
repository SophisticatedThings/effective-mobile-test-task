package artem.strelcov.apigateway.service;

import artem.strelcov.apigateway.dto.AuthenticationRequest;
import artem.strelcov.apigateway.dto.AuthenticationResponse;
import artem.strelcov.apigateway.dto.RegisterRequest;
import artem.strelcov.apigateway.dto.UserDTO;
import artem.strelcov.apigateway.model.User;
import artem.strelcov.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final WebClient.Builder webClient;

    public AuthenticationResponse register(RegisterRequest request) {

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var userDto = UserDTO.builder().username(request.getUsername()).build();

        WebClient.create("http://localhost:8085/api/subscriptions/replicate").post()
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwtToken))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(UserDTO.builder().username(request.getUsername()).build()), UserDTO.class)
                .retrieve()
                .bodyToMono(void.class)
                .block();

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findUserByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .build();
    }

    public void testing(UserDTO userDTO) {
        WebClient.create("http://localhost:8085/api/subscriptions/replicate").post()
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(UserDTO.builder().username("artem").build()), UserDTO.class)
                .retrieve()
                .bodyToMono(ResponseEntity.class)
                .block();
    }
}
