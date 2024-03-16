package io.github.joamik.cinema.domain;

import io.github.joamik.base.domain.Result;
import io.github.joamik.cinema.domain.ShowCommand.ReserveSeat;
import io.github.joamik.cinema.domain.ShowEvent.SeatReserved;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return Optional.ofNullable(seats.get(seatNumber))
                .<Result<ShowCommandError, List<ShowEvent>>>map(seat -> {
                    if (seat.isAvailable()) {
                        return Result.success(List.of(new SeatReserved(id, clock.instant(), seatNumber)));
                    } else {
                        return Result.failure(ShowCommandError.SEAT_NOT_AVAILABLE);
                    }
                })
                .orElseGet(() -> Result.failure(ShowCommandError.SEAT_NOT_EXISTS));
    }
}
