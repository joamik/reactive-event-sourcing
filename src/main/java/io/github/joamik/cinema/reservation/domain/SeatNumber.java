package io.github.joamik.cinema.reservation.domain;

import java.io.Serializable;

public record SeatNumber(Integer number) implements Serializable {

    public static SeatNumber of(int seatNumber) {
        return new SeatNumber(seatNumber);
    }
}
