package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.reservation.domain.ShowCommand.CancelSeatReservation;
import io.github.joamik.cinema.reservation.domain.ShowCommand.ReserveSeat;

import java.util.Random;
import java.util.Set;

public class ShowCommandFixture {

    private static final Random RANDOM = new Random();

    private static final int SEAT_NUMBER_BOUND = 11;

    public static ReserveSeat reserveRandomSeat(Show show) {
        return new ReserveSeat(show.id(), randomSeatNumber(show));
    }

    public static ReserveSeat reserveRandomSeat(ShowId showId) {
        return new ReserveSeat(showId, randomSeatNumber());
    }

    public static ReserveSeat reserveNotExistingSeat(Show show) {
        return new ReserveSeat(show.id(), notExistingSeatNumber(show));
    }

    public static CancelSeatReservation cancelRandomSeat(Show show) {
        return new CancelSeatReservation(show.id(), randomSeatNumber(show));
    }

    public static CancelSeatReservation cancelNotExistingSeatReservation(Show show) {
        return new CancelSeatReservation(show.id(), notExistingSeatNumber(show));
    }

    private static SeatNumber randomSeatNumber() {
        return SeatNumber.of(RANDOM.nextInt(1, SEAT_NUMBER_BOUND));
    }

    private static SeatNumber randomSeatNumber(Show show) {
        Set<SeatNumber> seatNumbers = show.seatNumbers();
        int randomSeat = RANDOM.nextInt(seatNumbers.size());
        return show.seatNumbers().toArray(new SeatNumber[seatNumbers.size()])[randomSeat];
    }

    private static SeatNumber notExistingSeatNumber(Show show) {
        Set<SeatNumber> seatNumbers = show.seatNumbers();
        int notExistingSeatNumber = seatNumbers.stream().mapToInt(SeatNumber::number).max().orElse(0) + 1;
        return SeatNumber.of(notExistingSeatNumber);
    }
}
