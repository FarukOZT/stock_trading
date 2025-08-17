package com.ing.brokerage.controller;

import com.ing.brokerage.model.Customer;
import com.ing.brokerage.model.Order;
import com.ing.brokerage.service.KeycloakCustomerService;
import com.ing.brokerage.service.OrderService;
import com.ing.brokerage.utils.JwtParser;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.AccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final KeycloakCustomerService keycloakCustomerService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            if (checkCustomer(order.getCustomerId())) {
                Order created = orderService.createOrder(
                        order.getCustomerId(),
                        order.getAssetName(),
                        order.getOrderSide(),
                        order.getSize(),
                        order.getPrice()
                );
                return ResponseEntity.ok(created);
            } else {
                return ResponseEntity.status(403).body("You don't have permission to show these orders.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/match")
    public ResponseEntity<Optional<Order>> matchOrder(@RequestBody Order order) {
        try {
            if(checkCustomer(order.getCustomerId())){
                Optional<Order> matchedOrder = orderService.matchOrder(order.getId());
                return ResponseEntity.ok(matchedOrder);
            } else {
                throw new AccessException("You dont have permission to show these orders.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (AccessException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@RequestParam Long customerId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            if (checkCustomer(customerId)) {
                return ResponseEntity.ok(orderService.getOrders(customerId, startDate, endDate));
            } else {
                return ResponseEntity.status(403).body("You don't have permission to show these orders.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> cancelOrder(@RequestParam Long orderId, @RequestParam Long customerId) {
        try {
            if (checkCustomer(customerId)) {
                return ResponseEntity.ok(orderService.cancelOrder(orderId));
            } else {
                return ResponseEntity.status(403).body("You don't have permission to show these orders.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    private boolean checkCustomer(Long id) {
        Map<String, String> map = JwtParser.jwtParser();
        String role = map.get("role");
        if (role.equalsIgnoreCase("admin")) {
            return true;
        } else {
            String keycloakCustomerId = map.get("userId");
            Optional<Customer> customer = keycloakCustomerService.getCustomer(keycloakCustomerId);
            if (customer.isPresent()) {
                if (customer.get().getId().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }
}

