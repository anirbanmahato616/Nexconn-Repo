package com.recoverx.commitment.exception;
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({CommitmentNotFoundException.class,CommitmentInstallmentNotFoundException.class})
    public ResponseEntity<ErrorResponse> notFound(RuntimeException ex,HttpServletRequest req){return build(HttpStatus.NOT_FOUND,ex.getMessage(),req);}
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> forbidden(org.springframework.security.access.AccessDeniedException ex,HttpServletRequest req){return build(HttpStatus.FORBIDDEN,ex.getMessage(),req);}
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> badRequest(BusinessRuleException ex,HttpServletRequest req){return build(HttpStatus.BAD_REQUEST,ex.getMessage(),req);}
    @ExceptionHandler(InvoiceIntegrationException.class)
    public ResponseEntity<ErrorResponse> integration(InvoiceIntegrationException ex,HttpServletRequest req){return build(HttpStatus.SERVICE_UNAVAILABLE,ex.getMessage(),req);}
    @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> validation(Exception ex,HttpServletRequest req){
        String message=ex instanceof MethodArgumentNotValidException m ? m.getBindingResult().getFieldErrors().stream().findFirst().map(e->e.getField()+": "+e.getDefaultMessage()).orElse("Validation failed") : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST,message,req);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception ex,HttpServletRequest req){return build(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected error occurred",req);}
    private ResponseEntity<ErrorResponse> build(HttpStatus status,String message,HttpServletRequest req){
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(),status.value(),status.getReasonPhrase(),message,req.getRequestURI()));
    }
}
