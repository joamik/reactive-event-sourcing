package io.github.joamik.cinema.reservation.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SeatsCreator {

    public static Map<SeatNumber, Seat> createSeats(BigDecimal price) {
        return IntStream.rangeClosed(1, 10)
                .mapToObj(number -> new Seat(new SeatNumber(number), SeatStatus.AVAILABLE, price))
                .collect(Collectors.toUnmodifiableMap(Seat::number, Function.identity()));
    }
}
