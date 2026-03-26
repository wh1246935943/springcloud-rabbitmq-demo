package com.demo.inventory.listener;

import com.demo.inventory.messaging.OrderMessage;
import com.demo.inventory.model.Product;
import com.demo.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryListener {

    private final ProductRepository productRepository;

    @RabbitListener(queues = "inventory.queue")
    @Transactional
    public void updateInventory(OrderMessage message) {
        log.info("Inventory service received order: orderId={}, productId={}, quantity={}",
                message.getOrderId(), message.getProductId(), message.getQuantity());

        productRepository.findById(message.getProductId()).ifPresentOrElse(
                product -> {
                    int newStock = product.getStock() - message.getQuantity();
                    if (newStock < 0) {
                        log.warn("Insufficient stock for product: id={}, available={}, requested={}",
                                product.getId(), product.getStock(), message.getQuantity());
                        return;
                    }
                    product.setStock(newStock);
                    productRepository.save(product);
                    log.info("Inventory updated: productId={}, newStock={}", product.getId(), newStock);
                },
                () -> log.warn("Product not found: id={}", message.getProductId())
        );
    }
}
