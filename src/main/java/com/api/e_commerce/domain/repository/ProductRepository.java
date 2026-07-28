package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Product;

import java.util.List;

public interface ProductRepository {

    Product save(Product product);
    List<Product> findAll();
}
