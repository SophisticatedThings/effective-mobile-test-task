package artem.strelcov.subscriptionsservice.exception_handling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<IncorrectData> userNotFoundException(
            NoSuchElementException e){
        IncorrectData data = new IncorrectData();
        data.setInformation(e.getMessage());
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);

    }
    @ExceptionHandler
    public ResponseEntity<IncorrectData> notFriendsException(
            NotFriendsException e){
        IncorrectData data = new IncorrectData();
        data.setInformation(e.getMessage());
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);

    }
    @ExceptionHandler
    public ResponseEntity<IncorrectData> noPostsException(
            NoPostsException e){
        IncorrectData data = new IncorrectData();
        data.setInformation(e.getMessage());
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);

    }
}
