package com.jai.router.examples;

import com.jai.router.core.*;
import com.jai.router.examples.RouteRequest;
import com.jai.router.examples.RouteResponse;
import com.jai.router.examples.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

/**
 * Global exception handler for the router API.
 */
@RestControllerAdvice
class RouterExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RouterExceptionHandler.class);
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(LlmClientException.class)
    public ResponseEntity<ErrorResponse> handleLlmError(LlmClientException ex) {
        log.error("LLM client error", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("LLM_ERROR", "Service temporarily unavailable"));
    }

    // Let ResponseEntityExceptionHandler handle general exceptions to avoid duplicate mappings.
    // Override the specific validation handler with the correct Spring 6 signature.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  org.springframework.http.HttpStatusCode status,
                                                                  WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(org.springframework.validation.FieldError::getDefaultMessage)
            .orElse("Invalid request payload");

        log.warn("Validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("INVALID_ARGUMENT", message));
    }
}

/**
 * REST controller for routing requests.
 */
@SpringBootApplication(scanBasePackages = "com.jai.router")
@RestController
@RequestMapping("/api/router")
public class ExampleApp {

    private static final Logger log = LoggerFactory.getLogger(ExampleApp.class);
    private final LlmClient client;

    public ExampleApp(LlmClient client) {
        this.client = client;
    }

    /**
     * Route a request to the appropriate service.
     * @param request the routing request
     * @return the routing decision with processing time
     */
    @PostMapping("/route")
    public ResponseEntity<RouteResponse> route(@Valid @RequestBody RouteRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            DecisionContext ctx = DecisionContext.of(request.payload());
            RoutingDecision decision = client.decide(ctx);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.info("Routing decision: service='{}' confidence={} processingTime={}ms",
                decision.service(), decision.confidence(), processingTime);
            
            return ResponseEntity.ok(new RouteResponse(
                decision.service(),
                decision.confidence(),
                decision.explanation(),
                processingTime
            ));
        } catch (Exception ex) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Routing failed after {}ms", processingTime, ex);
            throw ex;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }
}
