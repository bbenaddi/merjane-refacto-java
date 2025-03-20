package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class OrderProcessingServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderProcessingService orderProcessingService;

    private Order order;
    private Product normalProduct;
    private Product seasonalProduct;
    private Product expirableProduct;

    @BeforeEach
    void setUp() {
        // Setup normal product
        normalProduct = new Product();
        normalProduct.setType("NORMAL");
        normalProduct.setName("USB Cable");
        normalProduct.setAvailable(5);
        normalProduct.setLeadTime(3);

        // Setup seasonal product
        seasonalProduct = new Product();
        seasonalProduct.setType("SEASONAL");
        seasonalProduct.setName("Watermelon");
        seasonalProduct.setAvailable(2);
        seasonalProduct.setLeadTime(5);
        seasonalProduct.setSeasonStartDate(LocalDate.now().minusDays(10));
        seasonalProduct.setSeasonEndDate(LocalDate.now().plusDays(80));

        // Setup expirable product
        expirableProduct = new Product();
        expirableProduct.setType("EXPIRABLE");
        expirableProduct.setName("Milk");
        expirableProduct.setAvailable(3);
        expirableProduct.setLeadTime(2);
        expirableProduct.setExpiryDate(LocalDate.now().plusDays(30));

        // Setup order
        order = new Order();
        order.setId(1L);
        Set<Product> products = new HashSet<>();
        products.add(normalProduct);
        products.add(seasonalProduct);
        products.add(expirableProduct);
        order.setItems(products);
    }

    @Test
    void processOrder_NormalProduct_InStock() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderProcessingService.processOrder(1L);

        // Assert
        verify(productRepository, times(1)).save(normalProduct);
        assertEquals(4, normalProduct.getAvailable());
    }

    @Test
    void processOrder_NormalProduct_OutOfStock() {
        // Arrange
        normalProduct.setAvailable(0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderProcessingService.processOrder(1L);

        // Assert
        verify(productService).notifyDelay(3, normalProduct);
        verify(productRepository, never()).save(normalProduct);
    }

    @Test
    void processOrder_SeasonalProduct_InSeason_InStock() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderProcessingService.processOrder(1L);

        // Assert
        verify(productRepository, times(1)).save(seasonalProduct);
        assertEquals(1, seasonalProduct.getAvailable());
    }

    @Test
    void processOrder_SeasonalProduct_OutOfSeason() {
        // Arrange
        seasonalProduct.setSeasonStartDate(LocalDate.now().plusDays(10));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderProcessingService.processOrder(1L);

        // Assert
        verify(productService).handleSeasonalProduct(seasonalProduct);
        verify(productRepository, never()).save(seasonalProduct);
    }

    @Test
    void processOrder_ExpirableProduct_NotExpired_InStock() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderProcessingService.processOrder(1L);

        // Assert
        verify(productRepository, times(1)).save(expirableProduct);
        assertEquals(2, expirableProduct.getAvailable());
    }

    @Test
    void processOrder_ExpirableProduct_Expired() {
        // Arrange
        expirableProduct.setExpiryDate(LocalDate.now().minusDays(1));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderProcessingService.processOrder(1L);

        // Assert
        verify(productService).handleExpiredProduct(expirableProduct);
        verify(productRepository, never()).save(expirableProduct);
    }

    @Test
    void processOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderProcessingService.processOrder(999L);
        });
    }
}