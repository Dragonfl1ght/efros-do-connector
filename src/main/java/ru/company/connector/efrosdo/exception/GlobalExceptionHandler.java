package ru.company.connector.efrosdo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponseDto> handleUpstreamFailure(RestClientException ex) {
        if (ex instanceof RestClientResponseException responseEx) {
            log.error("Ошибка обращения к EDO или e4: HTTP {} {}",
                    responseEx.getStatusCode().value(), responseEx.getStatusText());
            log.debug("Полный стектрейс", ex);
        } else {
            log.error("Ошибка обращения к EDO или e4", ex);
        }
        return badGateway(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex) {
        log.error("Некорректный ответ EDO", ex);
        return badGateway(ex.getMessage());
    }

    private static ResponseEntity<ErrorResponseDto> badGateway(String message) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponseDto(message));
    }
}
