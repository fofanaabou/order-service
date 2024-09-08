package com.sinignaci.orderservice.web;

import com.sinignaci.orderservice.domain.Order;
import com.sinignaci.orderservice.service.OrderApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public Flux<Order> getAllOrders(@AuthenticationPrincipal Jwt jwt) {
        return orderApi.fetchAll(jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> submitOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return orderApi.submitOrder(orderRequest.isbn(), orderRequest.quantity());
    }

}
