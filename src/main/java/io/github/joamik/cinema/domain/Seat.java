package io.github.joamik.cinema.domain;

import java.math.BigDecimal;

public record Seat(SeatNumber number, SeatStatus status, BigDecimal price) {

}
