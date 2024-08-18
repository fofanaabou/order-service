package com.sinignaci.orderservice.book;

import lombok.Builder;

@Builder
public record Book(String isbn, String title, String author, Double price) {
}
