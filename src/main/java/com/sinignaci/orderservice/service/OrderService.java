package com.sinignaci.orderservice.service;


import com.sinignaci.orderservice.book.Book;
import com.sinignaci.orderservice.book.BookClient;
import com.sinignaci.orderservice.domain.Order;
import com.sinignaci.orderservice.domain.OrderStatus;
import com.sinignaci.orderservice.event.OrderAcceptedMessage;
import com.sinignaci.orderservice.event.OrderDispatchedMessage;
import com.sinignaci.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderApi {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);


    private final BookClient bookClient;
    private final OrderRepository orderRepository;
    private final StreamBridge streamBridge;

    @Override
    public Flux<Order> fetchAll() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn)
                .map(book -> buildAcceptedOrder(book, quantity))
                .defaultIfEmpty(buildRejectedOrder(isbn, quantity))
                .flatMap(orderRepository::save)
                .doOnNext(this::publishOrderAcceptedEvent);
    }

    @Override
    public Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux) {
        return flux
                .flatMap(message -> orderRepository.findById(message.orderId()))
                .map(OrderService::BuildDispatchedOrder)
                .flatMap(orderRepository::save);
    }

    public static Order buildRejectedOrder(String isbn, int quantity) {
        return Order.of(isbn, null, null, quantity, OrderStatus.REJECTED);
    }

    public static Order buildAcceptedOrder(Book book, int quantity) {
        String bookName = String.join(" - ", book.title(), book.author());
        return Order.of(book.isbn(), bookName, book.price(), quantity, OrderStatus.ACCEPTED);
    }

    private static Order BuildDispatchedOrder(Order existingOrder) {
        return existingOrder.toBuilder()
                .status(OrderStatus.DISPATCHED)
                .build();
    }

    private void publishOrderAcceptedEvent(Order order) {
        if (!order.status().equals(OrderStatus.ACCEPTED)) {
            return;
        }

        OrderAcceptedMessage orderAcceptedMessage = OrderAcceptedMessage.builder()
                .orderId(order.id())
                .build();

        log.info("Sending order accepted event with id: {}", order.id());
        boolean result = streamBridge.send("acceptOrder-out-0", orderAcceptedMessage);
        log.info("Result of sending data for order with id {}: {}", order.id(), result);
    }

}
