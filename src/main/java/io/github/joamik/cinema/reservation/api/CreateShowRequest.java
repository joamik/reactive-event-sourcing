package io.github.joamik.cinema.reservation.api;

import java.util.UUID;

public record CreateShowRequest(UUID id, String title, int maxSeats) {

}
