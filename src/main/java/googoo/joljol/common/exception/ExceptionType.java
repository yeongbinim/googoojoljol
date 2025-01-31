package googoo.joljol.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {

    SEARCH_TEXT_SHORT(HttpStatus.BAD_REQUEST, "S01", "검색어는 2글자 ");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
