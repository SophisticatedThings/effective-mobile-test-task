package artem.strelcov.subscriptionsservice.exception_handling;

public class NoPostsException extends RuntimeException{
    public NoPostsException(String message) {
        super(message);
    }
}
