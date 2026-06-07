package com.menu.demo.Exceptions;




import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.menu.demo.Enums.ExceptionType;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNotFound(NoHandlerFoundException ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.NOT_FOUND, ExceptionType.UNKNOWN_ERROR.getMessage(), ExceptionType.UNKNOWN_ERROR.getCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.UNAUTHORIZED, ExceptionType.ACCESS_DENIED.getMessage(), ExceptionType.ACCESS_DENIED.getCode());
    }

    /*@ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpiration(TokenExpiredException ex) {
        return buildResponse(HttpStatus., ExceptionType.ACCESS_DENIED.getMessage(), ExceptionType.ACCESS_DENIED.getCode());
    }*/

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleAuthNotFound(UsernameNotFoundException ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.OK, ExceptionType.BAD_CREDENTIALS.getMessage(), ExceptionType.BAD_CREDENTIALS.getCode());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleAuthNotFound2(BadCredentialsException ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.OK, ExceptionType.BAD_CREDENTIALS.getMessage(), ExceptionType.BAD_CREDENTIALS.getCode());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.BAD_REQUEST, ExceptionType.UNKNOWN_ERROR.getMessage(), ExceptionType.UNKNOWN_ERROR.getCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionType.UNKNOWN_ERROR.getMessage(), ExceptionType.UNKNOWN_ERROR.getCode());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, Integer code) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", code);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}

