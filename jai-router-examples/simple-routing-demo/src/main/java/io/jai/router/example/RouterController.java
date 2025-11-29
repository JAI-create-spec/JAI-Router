package io.jai.router.example;

import io.jai.router.core.Router;
import io.jai.router.core.RoutingResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouterController {

    private final Router router;


    public RouterController(ObjectProvider<Router> routerProvider) {
        this.router = routerProvider.getIfAvailable();
    }

    @PostMapping(path = "/api/router/route", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RoutingResult route(@RequestBody String request) {
        if (router == null) {
            return new RoutingResult("none", 0.0, "No router bean available");
        }
        return router.route(request);
    }
}
