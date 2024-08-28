package com.sinignaci.orderservice.event;

import lombok.Builder;

@Builder(toBuilder = true)
public record OrderAcceptedMessage(long orderId) {
}
