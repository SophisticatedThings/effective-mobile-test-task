package artem.strelcov.apigateway.exception_handling.UserHandling;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String message) {
        super(message);
    }

}
