package ru.urasha.callmeani.dream_marketplace.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.dto.ErrorResponse;

@RestControllerAdvice(assignableTypes = {LotController.class})
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : "Не удалось выполнить запрос";
        return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(message));
    }
}
