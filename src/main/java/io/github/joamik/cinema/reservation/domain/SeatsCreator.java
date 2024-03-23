package io.github.joamik.cinema.reservation.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

public class SeatsCreator {

    public static Map<SeatNumber, Seat> createSeats(BigDecimal price) {
        return IntStream.rangeClosed(1, 10)
                .mapToObj(number -> new Seat(new SeatNumber(number), SeatStatus.AVAILABLE, price))
                .collect(toUnmodifiableMap(Seat::number, identity()));
    }
}
