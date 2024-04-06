package io.github.joamik.cinema.reservation.api;

import io.github.joamik.cinema.reservation.domain.Show;

import java.util.List;

public record ShowResponse(String id, String title, List<SeatResponse> seats) {

    public static ShowResponse from(Show show) {
        var seatResponses = show.seats().values().stream()
                .map(SeatResponse::from)
                .toList();

        return new ShowResponse(show.id().id().toString(), show.title(), seatResponses);
    }
}
