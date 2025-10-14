package com.int371.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.int371.eventhub.dto.ApiResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handler for ResourceNotFoundException (Code 404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {

        ApiResponse<?> errorResponse = new ApiResponse<>(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handler for Validation (Code 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ApiResponse<?> errorResponse = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(), // 400
                errorMessage,                   // ข้อความที่เราตั้งไว้ใน DTO
                "Validation Failed",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handler for Exception ทั่วไป (Code 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex, HttpServletRequest request) {

        ApiResponse<?> errorResponse = new ApiResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handler for Bad Request (Code 400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {

        ApiResponse<?> errorResponse = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "Bad Request",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handler for RequestCooldownException (Code 429)
    @ExceptionHandler(RequestCooldownException.class)
    public ResponseEntity<ApiResponse<?>> handleRequestCooldownException(
            RequestCooldownException ex, HttpServletRequest request) {

        ApiResponse<?> errorResponse = new ApiResponse<>(
                HttpStatus.TOO_MANY_REQUESTS.value(), // 429
                ex.getMessage(),
                "Too Many Requests",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    // Handler Invalid email or password (Code 401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        ApiResponse<?> errorResponse = new ApiResponse<>(
                HttpStatus.UNAUTHORIZED.value(), // 401
                "Invalid email or password.",
                "Unauthorized",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handler Token (Code 401)
    @ExceptionHandler({MalformedJwtException.class, SignatureException.class, ExpiredJwtException.class})
    public ResponseEntity<ApiResponse<?>> handleJwtAuthenticationException(
            Exception ex, HttpServletRequest request) {

        String message;
        String error;

        if (ex instanceof ExpiredJwtException) {
            message = "Token has expired.";
            error = "Token Expired";
        } else {
            message = "Token is invalid or malformed.";
            error = "Invalid Token";
        }

        ApiResponse<?> errorResponse = new ApiResponse<>(
                HttpStatus.UNAUTHORIZED.value(), // 401
                message,
                error,
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handler for missed request body (Code 400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ApiResponse<?> errorResponse = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Required request body is missing or malformed.",
                "Bad Request",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}