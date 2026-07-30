package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Order;

public interface OrderRepository {

    Order save(Order order);
}
