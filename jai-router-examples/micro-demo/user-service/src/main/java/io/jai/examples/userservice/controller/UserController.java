package io.jai.examples.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Map<Long, Map<String, Object>> DB = new ConcurrentHashMap<>();
    static {
        DB.put(1L, Map.of("id", 1, "name", "Alice"));
        DB.put(2L, Map.of("id", 2, "name", "Bob"));
    }

    @GetMapping
    public Collection<Map<String, Object>> list() {
        return DB.values();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        var u = DB.get(id);
        return u != null ? ResponseEntity.ok(u) : ResponseEntity.notFound().build();
    }
}

