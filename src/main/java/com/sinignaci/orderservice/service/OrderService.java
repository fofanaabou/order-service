package com.sinignaci.orderservice.service;


import com.sinignaci.orderservice.domain.Order;
import com.sinignaci.orderservice.domain.OrderStatus;
import com.sinignaci.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderApi {

    private final OrderRepository orderRepository;

    @Override
    public Flux<Order> fetchAll() {
        return orderRepository.findAll();
    }

    @Override
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return orderRepository.save(buildRejectedOrder(isbn, quantity));
    }

    private static Order buildRejectedOrder(String isbn, int quantity) {
        return Order.of(isbn, null, null, quantity, OrderStatus.REJECTED);
    }


}
