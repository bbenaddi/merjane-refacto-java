package com.nimbleways.springboilerplate.config;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.OrderItems;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderItemsRepository;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Override
    public void run(String... args) {
        // Success cases
        Product normalProduct = new Product();
        normalProduct.setName("Normal Product (In Stock)");
        normalProduct.setType("NORMAL");
        normalProduct.setAvailable(5);
        normalProduct.setLeadTime(3);

        Product seasonalProduct = new Product();
        seasonalProduct.setName("Seasonal Product (In Season)");
        seasonalProduct.setType("SEASONAL");
        seasonalProduct.setAvailable(2);
        seasonalProduct.setLeadTime(5);
        seasonalProduct.setSeasonStartDate(LocalDate.now().minusDays(10));
        seasonalProduct.setSeasonEndDate(LocalDate.now().plusDays(80));

        Product expirableProduct = new Product();
        expirableProduct.setName("Expirable Product (Valid)");
        expirableProduct.setType("EXPIRABLE");
        expirableProduct.setAvailable(3);
        expirableProduct.setLeadTime(2);
        expirableProduct.setExpiryDate(LocalDate.now().plusDays(30));

        // Failure cases
        Product normalOutOfStock = new Product();
        normalOutOfStock.setName("Normal Product (Out of Stock)");
        normalOutOfStock.setType("NORMAL");
        normalOutOfStock.setAvailable(0);
        normalOutOfStock.setLeadTime(7);

        Product seasonalOutOfSeason = new Product();
        seasonalOutOfSeason.setName("Seasonal Product (Out of Season)");
        seasonalOutOfSeason.setType("SEASONAL");
        seasonalOutOfSeason.setAvailable(5);
        seasonalOutOfSeason.setLeadTime(10);
        seasonalOutOfSeason.setSeasonStartDate(LocalDate.now().plusDays(30)); // Season starts in future
        seasonalOutOfSeason.setSeasonEndDate(LocalDate.now().plusDays(90));

        Product expiredProduct = new Product();
        expiredProduct.setName("Expirable Product (Expired)");
        expiredProduct.setType("EXPIRABLE");
        expiredProduct.setAvailable(4);
        expiredProduct.setLeadTime(3);
        expiredProduct.setExpiryDate(LocalDate.now().minusDays(1)); // Already expired

        // Save all products
        productRepository.saveAll(Arrays.asList(
                normalProduct, seasonalProduct, expirableProduct,
                normalOutOfStock, seasonalOutOfSeason, expiredProduct));

        // Create orders to test both success and failure cases
        Order successOrder = new Order();
        Order failureOrder = new Order();
        orderRepository.saveAll(Arrays.asList(successOrder, failureOrder));

        // Create order items for success cases
        OrderItems normalItem = new OrderItems();
        normalItem.setOrder(successOrder);
        normalItem.setProduct(normalProduct);
        normalItem.setQuantity(1);

        OrderItems seasonalItem = new OrderItems();
        seasonalItem.setOrder(successOrder);
        seasonalItem.setProduct(seasonalProduct);
        seasonalItem.setQuantity(1);

        OrderItems expirableItem = new OrderItems();
        expirableItem.setOrder(successOrder);
        expirableItem.setProduct(expirableProduct);
        expirableItem.setQuantity(1);

        // Create order items for failure cases
        OrderItems normalFailItem = new OrderItems();
        normalFailItem.setOrder(failureOrder);
        normalFailItem.setProduct(normalOutOfStock);
        normalFailItem.setQuantity(1);

        OrderItems seasonalFailItem = new OrderItems();
        seasonalFailItem.setOrder(failureOrder);
        seasonalFailItem.setProduct(seasonalOutOfSeason);
        seasonalFailItem.setQuantity(1);

        OrderItems expirableFailItem = new OrderItems();
        expirableFailItem.setOrder(failureOrder);
        expirableFailItem.setProduct(expiredProduct);
        expirableFailItem.setQuantity(1);

        // Save all order items
        orderItemsRepository.saveAll(Arrays.asList(
                normalItem, seasonalItem, expirableItem,
                normalFailItem, seasonalFailItem, expirableFailItem));
    }
}