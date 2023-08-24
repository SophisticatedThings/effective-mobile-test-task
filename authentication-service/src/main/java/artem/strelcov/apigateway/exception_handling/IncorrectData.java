package artem.strelcov.apigateway.exception_handling;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class IncorrectData {
    private String information;
}
