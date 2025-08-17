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

import javax.servlet.http.HttpServletRequest;
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
    public Order createOrder(@RequestBody Order order) throws AccessException {
        if(checkCustomer(order.getCustomerId())){
            return orderService.createOrder(order.getCustomerId(),
                    order.getAssetName(), order.getOrderSide(), order.getSize(), order.getPrice());
        } else{
            throw new AccessException("You dont have permission to show these orders.");
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(@RequestParam Long customerId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) throws AccessException {
        if(checkCustomer(customerId)){
            return ResponseEntity.ok(orderService.getOrders(customerId, startDate, endDate));
        } else{
            throw new AccessException("You dont have permission to show these orders.");
        }
    }

    @DeleteMapping
    public ResponseEntity<Order> cancelOrder(@RequestParam Long orderId, @RequestParam Long customerId) throws AccessException {
        if(checkCustomer(customerId)){
            return ResponseEntity.ok(orderService.cancelOrder(orderId));
        } else{
            throw new AccessException("You dont have permission to show these orders.");
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

