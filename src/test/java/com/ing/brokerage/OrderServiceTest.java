package com.ing.brokerage;

import com.ing.brokerage.model.Asset;
import com.ing.brokerage.model.Customer;
import com.ing.brokerage.model.Order;
import com.ing.brokerage.model.enums.OrderSide;
import com.ing.brokerage.model.enums.OrderStatus;
import com.ing.brokerage.repository.CustomerRepository;
import com.ing.brokerage.repository.OrderRepository;
import com.ing.brokerage.service.AssetService;
import com.ing.brokerage.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private AssetService assetService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        customerRepository = mock(CustomerRepository.class);
        assetService = mock(AssetService.class);
        orderService = new OrderService(orderRepository, customerRepository, assetService);
    }

    @Test
    void testCreateOrder_Buy_Success() {
        Long customerId = 1L;
        String assetName = "BTC";
        double size = 2;
        double price = 100;
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setKeycloakId("asf12");
        customer.setUsername("faruk");
        customer.setPassword("pass");
        customer.setEmail("fark@gmail.com");
        customer.setRole("customer");
        Asset tryAsset = Asset.builder()
                .assetName("TRY")
                .usableSize(1000)
                .build();

        when(assetService.getAsset(customerId, "TRY")).thenReturn(tryAsset);
        when(customerRepository.findById(customerId)).thenReturn(Optional.ofNullable(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order order = orderService.createOrder(customerId, assetName, OrderSide.BUY, size, price);

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(assetName, order.getAssetName());
        assertEquals(customerId, order.getCustomerId());

        verify(assetService).updateUsableSize(customerId, "TRY", -size * price);
        verify(assetService).createAsset(any(Asset.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCreateOrder_Buy_InsufficientFunds() {
        Long customerId = 1L;
        String assetName = "ING";
        double size = 5;
        double price = 100;

        Asset tryAsset = Asset.builder()
                .assetName("TRY")
                .usableSize(100)
                .build();

        when(assetService.getAsset(customerId, "TRY")).thenReturn(tryAsset);

        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(customerId, assetName, OrderSide.BUY, size, price)
        );
    }

    @Test
    void testCreateOrder_Sell_Success() {
        Long customerId = 1L;
        String assetName = "ING";
        double size = 1;
        double price = 100;

        Asset asset = Asset.builder()
                .assetName(assetName)
                .usableSize(5)
                .build();

        when(assetService.getAsset(customerId, assetName)).thenReturn(asset);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order order = orderService.createOrder(customerId, assetName, OrderSide.SELL, size, price);

        assertEquals(OrderSide.SELL, order.getOrderSide());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        verify(assetService).updateUsableSize(customerId, assetName, -size);
        verify(assetService).updateUsableSize(customerId, "TRY", +size);
    }

    @Test
    void testCancelOrder_Success() {
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .customerId(1L)
                .assetName("ING")
                .orderSide(OrderSide.BUY)
                .size(2)
                .price(50)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order updatedOrder = orderService.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCELED, updatedOrder.getStatus());
        verify(assetService).updateUsableSize(order.getCustomerId(), "TRY", 100.0);
        verify(orderRepository).save(order);
    }

    @Test
    void testCancelOrder_OrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                orderService.cancelOrder(99L)
        );
    }

    @Test
    void testCancelOrder_NotPending() {
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatus.CANCELED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.cancelOrder(orderId)
        );
    }
}
