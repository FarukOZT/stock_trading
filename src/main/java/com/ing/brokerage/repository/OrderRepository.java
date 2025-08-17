package com.ing.brokerage.repository;

import com.ing.brokerage.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByCustomerIdAndCreateDateBetween(
            Long customerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
