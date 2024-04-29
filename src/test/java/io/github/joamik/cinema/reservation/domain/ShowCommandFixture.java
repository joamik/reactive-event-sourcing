package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.reservation.domain.ShowCommand.CancelSeatReservation;
import io.github.joamik.cinema.reservation.domain.ShowCommand.CreateShow;
import io.github.joamik.cinema.reservation.domain.ShowCommand.ReserveSeat;

import java.util.Random;
import java.util.Set;

public class ShowCommandFixture {

    private static final Random RANDOM = new Random();

    private static final int MAX_SEATS = 10;

    public static CreateShow randomCreateShow(ShowId showId) {
        return new CreateShow(showId, "Show title " + showId.id(), MAX_SEATS);
    }

    public static CreateShow randomCreateShow(ShowId showId, int maxSeats) {
        return new CreateShow(showId, "Show title " + showId.id(), maxSeats);
    }

    public static ReserveSeat randomReserveSeat(Show show) {
        return new ReserveSeat(show.id(), randomSeatNumber(show));
    }

    public static ReserveSeat randomReserveSeat(ShowId showId) {
        return new ReserveSeat(showId, randomSeatNumber());
    }

    public static ReserveSeat reserveNotExistingSeat(Show show) {
        return new ReserveSeat(show.id(), notExistingSeatNumber(show));
    }

    public static CancelSeatReservation randomCancelSeatReservation(Show show) {
        return new CancelSeatReservation(show.id(), randomSeatNumber(show));
    }

    public static CancelSeatReservation cancelNotExistingSeatReservation(Show show) {
        return new CancelSeatReservation(show.id(), notExistingSeatNumber(show));
    }

    private static SeatNumber randomSeatNumber() {
        return SeatNumber.of(RANDOM.nextInt(1, MAX_SEATS + 1));
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
