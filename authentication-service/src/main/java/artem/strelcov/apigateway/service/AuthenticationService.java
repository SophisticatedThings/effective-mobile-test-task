package artem.strelcov.apigateway.service;

import artem.strelcov.apigateway.dto.AuthenticationRequest;
import artem.strelcov.apigateway.dto.AuthenticationResponse;
import artem.strelcov.apigateway.dto.RegisterRequest;
import artem.strelcov.apigateway.dto.UserDto;
import artem.strelcov.apigateway.exception_handling.UserHandling.NotUniqueUsernameException;
import artem.strelcov.apigateway.exception_handling.UserHandling.UsernameNotFoundException;
import artem.strelcov.apigateway.model.User;
import artem.strelcov.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    /**
     * Функция создает пользователя. Webclient используется для того, чтобы
     * создать копию учетных данных для другого сервиса - subscriptions. Он будет их использовать
     * для большинства функционала. Подробнее смотрите сам сервис.
     */
    public void register(RegisterRequest request) {
        Optional<User> userCheck = userRepository
                .findUserByUsername(request.getUsername());
        if(userCheck.isPresent()) {
            throw new NotUniqueUsernameException("username занят, пожалуйста, введите другой");
        }
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);

        WebClient.create("http://subscriptions-service:8085/api/subscriptions/replicate")
                .post()
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwtToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(UserDto.builder()
                        .username(request.getUsername()).build()), UserDto.class)
                .retrieve()
                .bodyToMono(void.class)
                .block();

    }
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findUserByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Неправильный username"));

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .message("Используйте данный токен для любого запроса от лица данного пользователя")
                .build();
    }
}
