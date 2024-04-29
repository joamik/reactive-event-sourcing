package io.github.joamik.cinema.reservation.domain;

import java.io.Serializable;
import java.util.Map;

public record InitialShow(ShowId showId, String title, Map<SeatNumber, Seat> seats) implements Serializable {

}
