package com.sinignaci.orderservice.web;

import com.sinignaci.orderservice.domain.Order;
import com.sinignaci.orderservice.service.OrderApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApi orderApi;


    @GetMapping
    public Flux<Order> getAllOrders() {
        return orderApi.fetchAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> submitOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return orderApi.submitOrder(orderRequest.isbn(), orderRequest.quantity());
    }

}
