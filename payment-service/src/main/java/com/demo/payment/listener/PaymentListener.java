package com.demo.payment.listener;

import com.demo.payment.messaging.OrderMessage;
import com.demo.payment.model.Payment;
import com.demo.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentRepository paymentRepository;

    @RabbitListener(queues = "payment.queue")
    public void processPayment(OrderMessage message) {
        log.info("Payment service received order: orderId={}, product={}, amount={}",
                message.getOrderId(), message.getProductName(), message.getTotalAmount());

        try {
            // 模拟支付处理
            Thread.sleep(500);

            Payment payment = new Payment();
            payment.setOrderId(message.getOrderId());
            payment.setProductName(message.getProductName());
            payment.setAmount(message.getTotalAmount());
            payment.setStatus("PAID");

            paymentRepository.save(payment);
            log.info("Payment processed successfully: orderId={}", message.getOrderId());

        } catch (Exception e) {
            log.error("Payment processing failed for orderId={}: {}", message.getOrderId(), e.getMessage());

            Payment payment = new Payment();
            payment.setOrderId(message.getOrderId());
            payment.setProductName(message.getProductName());
            payment.setAmount(message.getTotalAmount());
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
        }
    }
}
