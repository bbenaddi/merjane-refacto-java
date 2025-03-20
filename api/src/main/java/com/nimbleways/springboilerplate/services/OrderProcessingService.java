package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Set;

@Service
@Transactional
public class OrderProcessingService {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found: " + orderId));

        Set<Product> products = order.getItems();
        for (Product p : products) {
            processProduct(p);
        }

        return new ProcessOrderResponse(order.getId());
    }

    private void processProduct(Product p) {
        if (p.getType().equals("NORMAL")) {
            processNormalProduct(p);
        } else if (p.getType().equals("SEASONAL")) {
            processSeasonalProduct(p);
        } else if (p.getType().equals("EXPIRABLE")) {
            processExpirableProduct(p);
        }
    }

    private void processNormalProduct(Product p) {
        if (p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            int leadTime = p.getLeadTime();
            if (leadTime > 0) {
                productService.notifyDelay(leadTime, p);
            }
        }
    }

    private void processSeasonalProduct(Product p) {
        LocalDate now = LocalDate.now();
        if (now.isAfter(p.getSeasonStartDate()) && now.isBefore(p.getSeasonEndDate())) {
            if (p.getAvailable() > 0) {
                p.setAvailable(p.getAvailable() - 1);
                productRepository.save(p);
            } else {
                LocalDate restockDate = now.plusDays(p.getLeadTime());
                if (restockDate.isBefore(p.getSeasonEndDate())) {
                    productService.notifyDelay(p.getLeadTime(), p);
                } else {
                    productService.handleSeasonalProduct(p);
                }
            }
        } else {
            productService.handleSeasonalProduct(p);
        }
    }

    private void processExpirableProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            productService.handleExpiredProduct(p);
        }
    }
}