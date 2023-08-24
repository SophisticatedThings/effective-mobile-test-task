package artem.strelcov.apigateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(

                description = "OpenApi документация для Authentication-service. Данный сервис " +
                        "используется для регистрации и аутентификации. В теле ответа после выполнения" +
                        " запроса на аутентификацию будет лежать токен, который необходимо скопировать " +
                        "и применять в остальных сервисах приложения ",
                title = "OpenApi Authentication-service specification - SophisticatedThings"
        ),
        servers = {
                @Server(
                        description = "authentication-service",
                        url = "http://localhost:8080"
                )
        }

)
public class OpenApiConfig {
}
