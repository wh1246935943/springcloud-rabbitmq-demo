package com.demo.inventory.data;

import com.demo.inventory.model.Product;
import com.demo.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            productRepository.save(new Product("iPhone 15 Pro", "苹果旗舰智能手机，A17 Pro芯片，钛金属机身", new BigDecimal("9999.00"), 100));
            productRepository.save(new Product("MacBook Pro 16寸", "苹果专业笔记本，M3 Max芯片，顶级性能", new BigDecimal("24999.00"), 50));
            productRepository.save(new Product("iPad Air", "苹果平板电脑，M1芯片，10.9寸液晶显示屏", new BigDecimal("4799.00"), 200));
            productRepository.save(new Product("AirPods Pro 2", "主动降噪无线耳机，H2芯片，MagSafe充电", new BigDecimal("1999.00"), 300));
            productRepository.save(new Product("Apple Watch Series 9", "智能手表，健康监测，双精度U1芯片", new BigDecimal("3299.00"), 150));
            productRepository.save(new Product("Mac mini", "迷你台式机，M2芯片，紧凑设计", new BigDecimal("4599.00"), 80));
            log.info("Inventory initialized with {} products", productRepository.count());
        }
    }
}
