package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.base.controll.Result.Success;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.domain.ShowCommand.ReserveSeat;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShowTest {

    private final Clock clock = Clock.fixed(Instant.parse("2024-03-16T21:32:05Z"));

    @Test
    void shouldReserveAvailableSeat() {
        // given
        var show = randomShow();
        var reserveSeat = randomReserveSeat();

        // when
        var result = show.process(reserveSeat, clock);

        // then
        assertThat(result).isInstanceOf(Success.class);

        // todo JM: is there simpler way to unpack events? how Vavr Either type does it?
        var events = ((Success<ShowCommandError, List<ShowEvent>>) result).value();
        assertThat(events).containsOnly(new SeatReserved(show.id(), clock.now(), reserveSeat.seatNumber()));
    }

    // todo JM: remaining test cases for reservation and cancellation

    private Show randomShow() {
        throw new UnsupportedOperationException(); // todo JM: Show fixtures?
    }

    private ReserveSeat randomReserveSeat() {
        throw new UnsupportedOperationException(); // todo JM: ShowCommand fixtures?
    }
}