package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.base.controll.Result;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.domain.ShowCommand.ReserveSeat;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.StringTemplate.STR;

public record Show(ShowId id, String title, Map<SeatNumber, Seat> seats) {

    private static final BigDecimal INITIAL_PRICE = BigDecimal.valueOf(100);

    public static Show create(ShowId showId) {
        return new Show(showId, "Show title " + showId.id(), SeatsCreator.createSeats(INITIAL_PRICE));
    }

    public Result<ShowCommandError, List<ShowEvent>> process(ShowCommand command, Clock clock) {
        return switch (command) {
            case ReserveSeat reserveSeat -> handleReservation(reserveSeat, clock);
        };
    }

    private Result<ShowCommandError, List<ShowEvent>> handleReservation(ReserveSeat reserveSeat, Clock clock) {
        SeatNumber seatNumber = reserveSeat.seatNumber();
        return getSeat(seatNumber)
                .<Result<ShowCommandError, List<ShowEvent>>>map(seat -> {
                    if (seat.isAvailable()) {
                        return Result.success(List.of(new SeatReserved(id, clock.now(), seatNumber)));
                    } else {
                        return Result.failure(ShowCommandError.SEAT_NOT_AVAILABLE);
                    }
                })
                .orElseGet(() -> Result.failure(ShowCommandError.SEAT_NOT_EXISTS));
    }

    public Show apply(ShowEvent showEvent) {
        return switch (showEvent) {
            case SeatReserved seatReserved -> applyReservation(seatReserved);
        };
    }

    private Show applyReservation(SeatReserved seatReserved) {
        SeatNumber seatNumber = seatReserved.seatNumber();
        Seat seat = getSeatOrElseThrow(seatNumber);

        Map<SeatNumber, Seat> newSeats = new HashMap<>(seats);
        newSeats.put(seatNumber, seat.reserved());

        return new Show(id, title, Collections.unmodifiableMap(newSeats));
    }

    private Seat getSeatOrElseThrow(SeatNumber seatNumber) {
        return getSeat(seatNumber)
                .orElseThrow(() -> new IllegalStateException(STR."Seat does not exist \{seatNumber}"));
    }

    private Optional<Seat> getSeat(SeatNumber seatNumber) {
        return Optional.ofNullable(seats.get(seatNumber));
    }
}
