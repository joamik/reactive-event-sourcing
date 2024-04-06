package io.github.joamik.cinema.reservation.api;

import io.github.joamik.cinema.reservation.domain.Seat;

import java.math.BigDecimal;

public record SeatResponse(int number, String status, BigDecimal price) {

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.number().number(), seat.status().name(), seat.price());
    }
}
