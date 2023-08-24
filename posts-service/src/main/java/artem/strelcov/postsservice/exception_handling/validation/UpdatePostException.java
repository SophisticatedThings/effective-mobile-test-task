package artem.strelcov.postsservice.exception_handling.validation;

public class UpdatePostException extends RuntimeException {
    public UpdatePostException(String message) {
        super(message);
    }

}