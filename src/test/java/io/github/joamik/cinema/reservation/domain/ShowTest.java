package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.base.controll.Result.Failure;
import io.github.joamik.cinema.base.controll.Result.Success;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.reserveNotExistingSeat;
import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.reserveRandomSeat;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShow;
import static org.assertj.core.api.Assertions.assertThat;

class ShowTest {

    private final Clock clock = Clock.fixed(Instant.parse("2024-03-16T21:32:05Z"));

    @Test
    void shouldReserveAvailableSeat() {
        // given
        var show = randomShow();
        var reserveSeat = reserveRandomSeat(show);

        // when
        var result = show.process(reserveSeat, clock);

        // then
        assertThat(result).isInstanceOf(Success.class);

        var events = ((Success<ShowCommandError, List<ShowEvent>>) result).value();
        assertThat(events).containsOnly(new SeatReserved(show.id(), clock.now(), reserveSeat.seatNumber()));
    }

    @Test
    void shouldApplyReservationOfAvailableSeat() {
        // given
        var show = randomShow();
        var reserveSeat = reserveRandomSeat(show);

        // when
        var events = ((Success<ShowCommandError, List<ShowEvent>>) show.process(reserveSeat, clock)).value();
        var updatedShow = apply(show, events);

        // then
        var reservedSeat = updatedShow.seats().get(reserveSeat.seatNumber());
        assertThat(reservedSeat.isReserved()).isTrue();
    }

    @Test
    void shouldNotReserveAlreadyReservedSeat() {
        // given
        var show = randomShow();
        var reserveSeat = reserveRandomSeat(show);

        // when
        var events = ((Success<ShowCommandError, List<ShowEvent>>) show.process(reserveSeat, clock)).value();
        var updatedShow = apply(show, events);

        // then
        var reservedSeat = updatedShow.seats().get(reserveSeat.seatNumber());
        assertThat(reservedSeat.isReserved()).isTrue();

        // when
        var result = updatedShow.process(reserveSeat, clock);

        // then
        assertThat(result).isInstanceOf(Failure.class);

        var error = ((Failure<ShowCommandError, List<ShowEvent>>) result).error();
        assertThat(error).isEqualTo(ShowCommandError.SEAT_NOT_AVAILABLE);
    }

    @Test
    void shouldNotReserveNotExistingSeat() {
        // given
        var show = randomShow();
        var reserveSeat = reserveNotExistingSeat(show);

        // when
        var result = show.process(reserveSeat, clock);

        // then
        assertThat(result).isInstanceOf(Failure.class);

        var error = ((Failure<ShowCommandError, List<ShowEvent>>) result).error();
        assertThat(error).isEqualTo(ShowCommandError.SEAT_NOT_EXISTS);
    }

    @Test
    void shouldCancelSeatReservation() {
        // todo JM
    }

    @Test
    void shouldApplySeatReservationCancellation() {
        // todo JM
    }

    @Test
    void shouldNotCancelReservationOfNotReservedSeat() {
        // todo JM
    }

    @Test
    void shouldNotCancelReservationOfNotExistingSeat() {
        // todo JM
    }

    private Show apply(Show show, List<ShowEvent> events) {
        var updatedShow = show;
        for (var event : events) {
            updatedShow = updatedShow.apply(event);
        }
        return updatedShow;
    }
}