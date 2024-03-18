package io.github.joamik.cinema.reservation.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ShowFixture {

    private static final Random RANDOM = new Random();

    private static final int PRICE_BOUND = 100;
    private static final int SEAT_NUMBER_BOUND = 10;

    public static Show randomShow() {
        return Show.create(randomShowId());
    }

    public static Show randomShowWithReservedSeats() {
        var seat = randomReservedSeat();
        return Show.create(randomShowId(), Map.of(seat.number(), seat));
    }

    private static ShowId randomShowId() {
        return new ShowId(UUID.randomUUID());
    }

    private static Seat randomReservedSeat() {
        return new Seat(randomSeatNumber(), SeatStatus.RESERVED, randomPrice());
    }

    private static SeatNumber randomSeatNumber() {
        return new SeatNumber(RANDOM.nextInt(SEAT_NUMBER_BOUND));
    }

    private static BigDecimal randomPrice() {
        return BigDecimal.valueOf(RANDOM.nextInt(PRICE_BOUND));
    }
}
