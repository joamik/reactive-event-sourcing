package io.github.joamik.cinema.domain;

import java.math.BigDecimal;
import java.util.Map;

public record Show(ShowId id, String title, Map<SeatNumber, Seat> seats) {

    private static final BigDecimal INITIAL_PRICE = BigDecimal.valueOf(100);

    public static Show create(ShowId showId) {
        return new Show(showId, "Show title " + showId.id(), SeatsCreator.createSeats(INITIAL_PRICE));
    }
}
