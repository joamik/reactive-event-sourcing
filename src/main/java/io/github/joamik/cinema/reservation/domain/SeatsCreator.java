package io.github.joamik.cinema.reservation.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

public class SeatsCreator {

    @Deprecated(forRemoval = true)
    public static Map<SeatNumber, Seat> createSeats(BigDecimal price) {
        return createSeats(price, 10);
    }

    public static Map<SeatNumber, Seat> createSeats(BigDecimal price, int maxSeats) {
        return IntStream.rangeClosed(1, maxSeats)
                .mapToObj(number -> new Seat(SeatNumber.of(number), SeatStatus.AVAILABLE, price))
                .collect(toUnmodifiableMap(Seat::number, identity()));
    }
}
