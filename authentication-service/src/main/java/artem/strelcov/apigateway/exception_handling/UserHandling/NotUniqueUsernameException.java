package artem.strelcov.apigateway.exception_handling.UserHandling;

public class NotUniqueUsernameException extends RuntimeException{
    public NotUniqueUsernameException(String message) {
        super(message);
    }
}
