package com.ing.brokerage.controller;

import com.ing.brokerage.model.Customer;
import com.ing.brokerage.service.KeycloakCustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final KeycloakCustomerService keycloakCustomerService;

    public CustomerController(KeycloakCustomerService keycloakCustomerService) {
        this.keycloakCustomerService = keycloakCustomerService;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        customer = keycloakCustomerService.createCustomerInKeycloak(customer);
        return ResponseEntity.ok(customer);
    }
}
