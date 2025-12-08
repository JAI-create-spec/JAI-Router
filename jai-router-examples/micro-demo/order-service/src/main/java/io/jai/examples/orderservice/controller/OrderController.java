package io.jai.examples.orderservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Map<Long, Map<String, Object>> DB = new ConcurrentHashMap<>();
    private static final AtomicLong SEQ = new AtomicLong(1);

    @GetMapping
    public Collection<Map<String, Object>> list() {
        return DB.values();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        var o = DB.get(id);
        return o != null ? ResponseEntity.ok(o) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> payload) {
        long id = SEQ.getAndIncrement();
        var order = new HashMap<>(payload);
        order.put("id", id);
        DB.put(id, order);
        return ResponseEntity.ok(order);
    }
}

