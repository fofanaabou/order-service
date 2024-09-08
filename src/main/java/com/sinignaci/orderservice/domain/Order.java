package com.sinignaci.orderservice.domain;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Table("orders")
public record Order(
        @Id
        @With
        Long id,
        String bookIsbn,
        String bookName,
        Double bookPrice,
        Integer quantity,
        OrderStatus status,
        @CreatedDate
        @With
        LocalDateTime createdDate,
        @LastModifiedDate
        @With
        LocalDateTime lastModifiedDate,
        @CreatedBy
        @With
        String createdBy,
        @LastModifiedBy
        String lastModifiedBy,
        @Version
        int version
) {
    public static Order of(String bookIsbn, String bookName, Double bookPrice, Integer quantity, OrderStatus orderStatus) {
        return new Order(null, bookIsbn, bookName, bookPrice, quantity, orderStatus, LocalDateTime.now(), LocalDateTime.now(), null, null, 0);
    }
}
