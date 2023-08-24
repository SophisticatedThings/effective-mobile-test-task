package artem.strelcov.apigateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    @NotBlank(message = "Требуется ввести username")
    private String username;
    //@Size(min = 8,max = 20,message = "Пароль должен быть не менее 8 символов и не более 20")
    @NotBlank(message = "Требуется ввести пароль")
    private String password;
}