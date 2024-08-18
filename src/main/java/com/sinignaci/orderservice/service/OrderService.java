package com.sinignaci.orderservice.service;


import com.sinignaci.orderservice.book.Book;
import com.sinignaci.orderservice.book.BookClient;
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

    private final BookClient bookClient;
    private final OrderRepository orderRepository;

    @Override
    public Flux<Order> fetchAll() {
        return orderRepository.findAll();
    }

    @Override
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn)
                .map(book -> buildAcceptedOrder(book, quantity))
                .defaultIfEmpty(buildRejectedOrder(isbn, quantity))
                .flatMap(orderRepository::save);
    }

    public static Order buildRejectedOrder(String isbn, int quantity) {
        return Order.of(isbn, null, null, quantity, OrderStatus.REJECTED);
    }

    public static Order buildAcceptedOrder(Book book, int quantity) {
        String bookName = String.join(" - ", book.title(), book.author());
        return Order.of(book.isbn(), bookName, book.price(), quantity, OrderStatus.ACCEPTED);
    }

}
