package com.demo.order.service;

import com.demo.order.config.RabbitMQConfig;
import com.demo.order.messaging.OrderMessage;
import com.demo.order.model.Order;
import com.demo.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Order createOrder(Long productId, String productName, Integer quantity, BigDecimal price) {
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(quantity));

        Order order = new Order();
        order.setProductId(productId);
        order.setProductName(productName);
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: id={}, product={}, quantity={}", savedOrder.getId(), productName, quantity);

        // 发布消息到 RabbitMQ
        OrderMessage message = new OrderMessage(
                savedOrder.getId(),
                productId,
                productName,
                quantity,
                price,
                totalAmount
        );
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_ROUTING_KEY,
                    message
            );
            log.info("Order message sent to RabbitMQ: orderId={}", savedOrder.getId());
        } catch (AmqpException ex) {
            log.error("Failed to send order message to RabbitMQ, orderId={}", savedOrder.getId(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error sending order message, orderId={}", savedOrder.getId(), ex);
        }

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
