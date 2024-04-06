package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.base.domain.Result;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.domain.ShowCommand.CancelSeatReservation;
import io.github.joamik.cinema.reservation.domain.ShowCommand.ReserveSeat;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReservationCancelled;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.StringTemplate.STR;

public record Show(ShowId id, String title, Map<SeatNumber, Seat> seats) implements Serializable {

    private static final BigDecimal INITIAL_PRICE = BigDecimal.valueOf(100);

    public Set<SeatNumber> seatNumbers() {
        return seats.keySet();
    }

    public static Show create(ShowId showId) {
        return create(showId, SeatsCreator.createSeats(INITIAL_PRICE));
    }

    public static Show create(ShowId showId, Map<SeatNumber, Seat> seats) {
        return new Show(showId, "Show title " + showId.id(), seats);
    }

    public Result<ShowCommandError, List<ShowEvent>> process(ShowCommand command, Clock clock) {
        return switch (command) {
            case ReserveSeat reserveSeat -> handleReservation(reserveSeat, clock);
            case CancelSeatReservation cancelSeatReservation -> handleReservationCancellation(cancelSeatReservation, clock);
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

    private Result<ShowCommandError, List<ShowEvent>> handleReservationCancellation(CancelSeatReservation cancelSeatReservation, Clock clock) {
        SeatNumber seatNumber = cancelSeatReservation.seatNumber();
        return getSeat(seatNumber)
                .<Result<ShowCommandError, List<ShowEvent>>>map(seat -> {
                    if (seat.isReserved()) {
                        return Result.success(List.of(new SeatReservationCancelled(id, clock.now(), seatNumber)));
                    } else {
                        return Result.failure(ShowCommandError.SEAT_NOT_RESERVED);
                    }
                })
                .orElseGet(() -> Result.failure(ShowCommandError.SEAT_NOT_EXISTS));
    }

    public Show apply(ShowEvent showEvent) {
        return switch (showEvent) {
            case SeatReserved seatReserved -> applyReservation(seatReserved);
            case SeatReservationCancelled seatReservationCancelled -> applyReservationCancellation(seatReservationCancelled);
        };
    }

    private Show applyReservationCancellation(SeatReservationCancelled seatReservationCancelled) {
        SeatNumber seatNumber = seatReservationCancelled.seatNumber();
        Seat seat = getSeatOrElseThrow(seatNumber);

        Map<SeatNumber, Seat> newSeats = new HashMap<>(seats);
        newSeats.put(seatNumber, seat.available());

        return new Show(id, title, Collections.unmodifiableMap(newSeats));
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
