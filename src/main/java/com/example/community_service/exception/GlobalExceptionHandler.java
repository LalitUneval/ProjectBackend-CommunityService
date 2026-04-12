package com.example.community_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> builderResponse(String message, HttpStatus status){

        Map<String, Object> response = new HashMap<>();

        response.put("Timestamp:", LocalDateTime.now());
        response.put("Message:", message);

        response.put("Status:", status);

        return new ResponseEntity<>(response, status);

    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }


    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(GroupNotFoundException ex) {
        // Condition: When service layer cannot find a record, this triggers.
        return builderResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(GroupAlreadyExistsException.class)
    public ResponseEntity<Object> handleNotFound(GroupAlreadyExistsException ex) {
        // Condition: When service layer cannot find a record, this triggers.
        return builderResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(PostNotFoundException ex) {
        // Condition: When service layer cannot find a record, this triggers.
        return builderResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleNotFound(UnauthorizedException ex) {
        // Condition: When service layer cannot find a record, this triggers.
        return builderResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

}
