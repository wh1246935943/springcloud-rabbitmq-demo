package com.demo.order.controller;

import com.demo.order.model.Order;
import com.demo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            if (request == null
                    || !request.containsKey("productId")
                    || !request.containsKey("productName")
                    || !request.containsKey("quantity")
                    || !request.containsKey("price")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "missing required fields"));
            }

            Long productId = Long.valueOf(request.get("productId").toString());
            String productName = request.get("productName").toString();
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            BigDecimal price = new BigDecimal(request.get("price").toString());

            Order order = orderService.createOrder(productId, productName, quantity, price);
            return ResponseEntity.ok(order);
        } catch (NumberFormatException | NullPointerException ex) {
            log.warn("Invalid createOrder request payload: {}", request, ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "invalid input", "message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("createOrder failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "internal server error"));
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.get("status")));
    }
}
