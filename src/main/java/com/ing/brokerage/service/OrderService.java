package com.ing.brokerage.service;

import com.ing.brokerage.model.Asset;
import com.ing.brokerage.model.Order;
import com.ing.brokerage.model.enums.OrderSide;
import com.ing.brokerage.model.enums.OrderStatus;
import com.ing.brokerage.repository.CustomerRepository;
import com.ing.brokerage.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AssetService assetService;

    public Order createOrder(Long customerId, String assetName, OrderSide side, double size, double price) {

        if (side == OrderSide.BUY) {
            double requiredTRY = size * price;
            Asset tryAsset = assetService.getAsset(customerId, "TRY");
            if (tryAsset == null || tryAsset.getUsableSize() < requiredTRY) {
                throw new IllegalArgumentException("Insufficient amount of TRY");
            }
            assetService.updateUsableSize(customerId, "TRY", -requiredTRY);
            Asset boughtAsset = Asset.builder()
                    .assetName(assetName)
                    .usableSize(size)
                    .size(size)
                    .customer(customerRepository.findById(customerId).get())
                    .build();
            assetService.createAsset(boughtAsset);
        } else {
            Asset asset = assetService.getAsset(customerId, assetName);
            if (asset == null || asset.getUsableSize() < size) {
                throw new IllegalArgumentException("Insufficient amount of assets");
            }
            assetService.updateUsableSize(customerId, assetName, -size);
            assetService.updateUsableSize(customerId, "TRY", +size);
        }

        Order order = Order.builder()
                .customerId(customerId)
                .assetName(assetName)
                .orderSide(side)
                .size(size)
                .price(price)
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }

    public List<Order> getOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only orders with PENDING status can be canceled");
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            double refundTRY = order.getSize() * order.getPrice();
            assetService.updateUsableSize(order.getCustomerId(), "TRY", refundTRY);
        } else {
            assetService.updateUsableSize(order.getCustomerId(), order.getAssetName(), order.getSize());
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        return order;
    }
}
