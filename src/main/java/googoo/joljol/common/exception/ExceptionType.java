package googoo.joljol.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {

    SHOPPING_MALL_NOT_FOUND(HttpStatus.NOT_FOUND, "S01", "해당 쇼핑몰을 찾을 수 없습니다."),
    SHOPPING_MALL_STATS_NOT_FOUND(HttpStatus.NOT_FOUND, "S02", "쇼핑몰 조회수 데이터를 찾을 수 없습니다."),
    SHOPPING_MALL_HOT_RANK_BAD_REQUEST(HttpStatus.BAD_REQUEST, "S03", "인기 쇼핑몰 노출 범위는 10 ~ 100개입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
