package io.github.joamik.cinema.reservation.domain;

import java.time.Instant;

public sealed interface ShowEvent {

    ShowId showId();

    Instant createdAt();

    record SeatReserved(ShowId showId, Instant createdAt, SeatNumber seatNumber) implements ShowEvent {

    }

    record SeatReservationCancelled(ShowId showId, Instant createdAt, SeatNumber seatNumber) implements ShowEvent {

    }
}
