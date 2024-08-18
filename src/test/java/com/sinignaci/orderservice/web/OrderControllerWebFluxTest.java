package com.sinignaci.orderservice.web;

import com.sinignaci.orderservice.domain.Order;
import com.sinignaci.orderservice.domain.OrderStatus;
import com.sinignaci.orderservice.service.OrderApi;
import com.sinignaci.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebFluxTest(OrderController.class)
class OrderControllerWebFluxTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderApi orderApi;

    @Test
    void whenBookNotAvailableThenRejectOrder() {
        var orderRequest = OrderRequest.builder()
                .isbn("1234567890")
                .quantity(4)
                .build();

        var expectedOrder = OrderService.buildRejectedOrder(orderRequest.isbn(), orderRequest.quantity());
        given(orderApi.submitOrder(orderRequest.isbn(), orderRequest.quantity())).willReturn(Mono.just(expectedOrder));

        webTestClient.post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Order.class).value(actual -> {
                    assertThat(actual).isNotNull();
                    assertThat(actual.status()).isEqualTo(OrderStatus.REJECTED);
                });
    }
}