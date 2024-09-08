package com.sinignaci.orderservice.service;

import com.sinignaci.orderservice.domain.Order;
import com.sinignaci.orderservice.event.OrderDispatchedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderApi {
    Flux<Order> fetchAll(String userId);

    Mono<Order> submitOrder(String isbn, int quantity);
    Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux);
}
