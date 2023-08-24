package artem.strelcov.postsservice.exception_handling;

import artem.strelcov.postsservice.exception_handling.validation.GetPostsException;
import artem.strelcov.postsservice.exception_handling.validation.UpdatePostException;
import artem.strelcov.postsservice.exception_handling.validation.ValidationErrorResponse;
import artem.strelcov.postsservice.exception_handling.validation.Violation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse onConstraintValidationException(
            ConstraintViolationException e
    ) {
        final List<Violation> violations = e.getConstraintViolations().stream()
                .map(
                        violation -> new Violation(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                )
                .collect(Collectors.toList());
        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        final List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        return new ValidationErrorResponse(violations);
    }
    @ExceptionHandler
    public ResponseEntity<IncorrectData> updatePostException(
             UpdatePostException e){
        IncorrectData data = new IncorrectData();
        data.setInformation(e.getMessage());
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);

    }
    @ExceptionHandler
    public ResponseEntity<IncorrectData> getPostsException(
            GetPostsException e){
        IncorrectData data = new IncorrectData();
        data.setInformation(e.getMessage());
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);

    }

}
