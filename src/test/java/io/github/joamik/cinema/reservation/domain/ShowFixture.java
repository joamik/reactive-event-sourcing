package io.github.joamik.cinema.reservation.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ShowFixture {

    private static final Random RANDOM = new Random();

    private static final int MAX_PRICE = 100;
    private static final int MAX_SEATS = 10;

    public static Show randomShow() {
        var showId = randomShowId();
        var seats = SeatsCreator.createSeats(randomPrice(), MAX_SEATS);
        return new Show(showId, "Show title " + showId.id(), seats);
    }

    public static Show randomShowWithReservedSeats() {
        var showId = randomShowId();
        var seat = randomReservedSeat();
        var seats = Map.of(seat.number(), seat);
        return new Show(showId, "Show title " + showId.id(), seats);
    }

    public static ShowId randomShowId() {
        return ShowId.of(UUID.randomUUID());
    }

    public static SeatNumber randomSeatNumber() {
        return SeatNumber.of(RANDOM.nextInt(1, MAX_SEATS + 1));
    }

    private static Seat randomReservedSeat() {
        return new Seat(randomSeatNumber(), SeatStatus.RESERVED, randomPrice());
    }

    private static BigDecimal randomPrice() {
        return BigDecimal.valueOf(RANDOM.nextInt(1, MAX_PRICE + 1));
    }
}
