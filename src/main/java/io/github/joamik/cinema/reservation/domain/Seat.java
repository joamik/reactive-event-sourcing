package io.github.joamik.cinema.reservation.domain;

import java.math.BigDecimal;

public record Seat(SeatNumber number, SeatStatus status, BigDecimal price) {

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public Seat reserved() {
        return new Seat(number, SeatStatus.RESERVED, price);
    }
}
