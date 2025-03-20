package com.nimbleways.springboilerplate.repositories;

import com.nimbleways.springboilerplate.entities.OrderItems;
import com.nimbleways.springboilerplate.entities.OrderItemsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, OrderItemsId> {
    List<OrderItems> findByOrderId(Long orderId);

    List<OrderItems> findByProductId(Long productId);
}