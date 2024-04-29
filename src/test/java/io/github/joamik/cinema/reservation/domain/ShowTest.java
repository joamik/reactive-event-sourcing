package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.base.domain.Result.Failure;
import io.github.joamik.cinema.base.domain.Result.Success;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReservationCancelled;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.cancelNotExistingSeatReservation;
import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.randomCancelSeatReservation;
import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.randomCreateShow;
import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.reserveNotExistingSeat;
import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.randomReserveSeat;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShow;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowId;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowWithReservedSeats;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ShowTest {

    private final Clock clock = Clock.fixed(Instant.parse("2024-03-16T21:32:05Z"));

    @Test
    void shouldCreateShow() {
        // given
        var showId = randomShowId();
        var createShow = randomCreateShow(showId);

        // when
        var result = (Success<ShowCommandError, ShowCreated>) ShowCreator.create(createShow, clock);
        var show = Show.create(result.value());

        // then
        assertThat(show.id()).isEqualTo(showId);
        assertThat(show.title()).isEqualTo(createShow.title());
        assertThat(show.seats()).hasSize(createShow.maxSeats());
    }

    @Test
    void shouldNotProcessCreateShowCommandForExistingShow() {
        // given
        var show = randomShow();
        var createShow = randomCreateShow(show.id());

        // when
        var result = show.process(createShow, clock);

        // then
        var error = ((Failure<ShowCommandError, List<ShowEvent>>) result).error();
        assertThat(error).isEqualTo(ShowCommandError.SHOW_ALREADY_EXISTS);
    }

    @Test
    void shouldNotApplyShowCreatedOnExistingShow() {
        // given
        var show = randomShow();
        var createShow = randomCreateShow(show.id());
        var result = (Success<ShowCommandError, ShowCreated>) ShowCreator.create(createShow, clock);
        var showCreated = result.value();

        // when
        var throwable = catchThrowable(() -> show.apply(showCreated));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReserveAvailableSeat() {
        // given
        var show = randomShow();
        var reserveSeat = randomReserveSeat(show);

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
        var reserveSeat = randomReserveSeat(show);

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
        var reserveSeat = randomReserveSeat(show);

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
        // given
        var show = randomShowWithReservedSeats();
        var cancelSeatReservation = randomCancelSeatReservation(show);

        // when
        var result = show.process(cancelSeatReservation, clock);

        // then
        assertThat(result).isInstanceOf(Success.class);

        var events = ((Success<ShowCommandError, List<ShowEvent>>) result).value();
        assertThat(events).containsOnly(new SeatReservationCancelled(show.id(), clock.now(), cancelSeatReservation.seatNumber()));
    }

    @Test
    void shouldApplySeatReservationCancellation() {
        // given
        var show = randomShowWithReservedSeats();
        var cancelSeatReservation = randomCancelSeatReservation(show);

        // when
        var events = ((Success<ShowCommandError, List<ShowEvent>>) show.process(cancelSeatReservation, clock)).value();
        var updatedShow = apply(show, events);

        // then
        var canceledSeat = updatedShow.seats().get(cancelSeatReservation.seatNumber());
        assertThat(canceledSeat.isAvailable()).isTrue();
    }

    @Test
    void shouldNotCancelReservationOfNotReservedSeat() {
        var show = randomShow();
        var cancelSeatReservation = randomCancelSeatReservation(show);

        // when
        var result = show.process(cancelSeatReservation, clock);

        // then
        assertThat(result).isInstanceOf(Failure.class);

        var error = ((Failure<ShowCommandError, List<ShowEvent>>) result).error();
        assertThat(error).isEqualTo(ShowCommandError.SEAT_NOT_RESERVED);
    }

    @Test
    void shouldNotCancelReservationOfNotExistingSeat() {
        // given
        var show = randomShow();
        var cancelSeatReservation = cancelNotExistingSeatReservation(show);

        // when
        var result = show.process(cancelSeatReservation, clock);

        // then
        assertThat(result).isInstanceOf(Failure.class);

        var error = ((Failure<ShowCommandError, List<ShowEvent>>) result).error();
        assertThat(error).isEqualTo(ShowCommandError.SEAT_NOT_EXISTS);
    }

    private Show apply(Show show, List<ShowEvent> events) {
        var updatedShow = show;
        for (var event : events) {
            updatedShow = updatedShow.apply(event);
        }
        return updatedShow;
    }
}