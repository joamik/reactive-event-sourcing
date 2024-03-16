package io.github.joamik.cinema.domain;

import java.time.Instant;

public sealed interface ShowEvent {

    ShowId showId();

    Instant createdAt();

    record SeatReserved(ShowId showId, Instant createdAt, SeatNumber seatNumber) implements ShowEvent {

    }
}
