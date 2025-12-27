package io.jai.router.example;

import io.jai.router.core.Router;
import io.jai.router.core.RoutingResult;
import io.jai.router.core.exception.InvalidInputException;
import io.jai.router.core.validation.RoutingInputValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RouterController {

    private static final Logger log = LoggerFactory.getLogger(RouterController.class);

    private final Router router;
    private final RoutingInputValidator validator;

    public RouterController(@NotNull ObjectProvider<Router> routerProvider,
                           @NotNull ObjectProvider<RoutingInputValidator> validatorProvider) {
        this.router = routerProvider.getIfAvailable();
        this.validator = validatorProvider.getIfAvailable(RoutingInputValidator::new);
    }

    @PostMapping(path = "/api/router/route", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull
    public ResponseEntity<?> route(@NotNull @RequestBody Object requestBody) {
        long start = System.currentTimeMillis();

        String payload = extractPayload(requestBody);
        if (payload == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "payload is required"));
        }

        try {
            validator.validate(payload);
            String sanitized = validator.sanitize(payload);
            if (router == null) {
                log.info("No Router bean available - returning fallback result");
                return ResponseEntity.ok(RoutingResult.of("none", 0.0, "No router bean available"));
            }

            RoutingResult result = router.route(sanitized);
            long time = System.currentTimeMillis() - start;
            log.debug("Routed request to={} confidence={} timeMs={}", result.service(), result.confidence(), time);
            return ResponseEntity.ok(result);
        } catch (InvalidInputException iie) {
            log.warn("Invalid routing request: {}", iie.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", iie.getMessage()));
        } catch (RuntimeException ex) {
            log.error("Routing failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "routing_failed", "message", ex.getMessage()));
        }
    }

    @Nullable
    private String extractPayload(@NotNull Object requestBody) {
        if (requestBody instanceof String) {
            return (String) requestBody;
        }
        if (requestBody instanceof Map) {
            Object payload = ((Map<?, ?>) requestBody).get("payload");
            if (payload instanceof String) {
                return (String) payload;
            }
            return payload == null ? null : payload.toString();
        }
        return requestBody.toString();
    }
}
