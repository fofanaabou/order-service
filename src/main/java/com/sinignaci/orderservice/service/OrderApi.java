package com.sinignaci.orderservice.service;

import com.sinignaci.orderservice.domain.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderApi {
    Flux<Order> fetchAll();

    Mono<Order> submitOrder(String isbn, int quantity);
}
