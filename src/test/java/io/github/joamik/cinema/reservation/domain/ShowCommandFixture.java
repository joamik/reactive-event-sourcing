package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.reservation.domain.ShowCommand.CancelSeatReservation;
import io.github.joamik.cinema.reservation.domain.ShowCommand.ReserveSeat;

import java.util.Random;
import java.util.Set;

public class ShowCommandFixture {

    private static final Random RANDOM = new Random();

    public static ReserveSeat reserveRandomSeat(Show show) {
        return new ReserveSeat(show.id(), randomSeatNumber(show));
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

    private static SeatNumber randomSeatNumber(Show show) {
        Set<SeatNumber> seatNumbers = show.seatNumbers();
        int randomSeat = RANDOM.nextInt(seatNumbers.size());
        return show.seatNumbers().toArray(new SeatNumber[seatNumbers.size()])[randomSeat];
    }

    private static SeatNumber notExistingSeatNumber(Show show) {
        Set<SeatNumber> seatNumbers = show.seatNumbers();
        Integer notExistingSeatNumber = seatNumbers.stream().mapToInt(SeatNumber::number).max().orElse(0) + 1;
        return new SeatNumber(notExistingSeatNumber);
    }
}
