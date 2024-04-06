package io.github.joamik.cinema.reservation.api;

public record SeatActionRequest(Action action) {

    enum Action {
        RESERVE,
        CANCEL_RESERVATION
    }
}
