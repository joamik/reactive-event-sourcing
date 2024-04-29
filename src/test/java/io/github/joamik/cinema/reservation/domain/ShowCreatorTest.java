package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.base.domain.Result;
import io.github.joamik.cinema.base.domain.Result.Failure;
import io.github.joamik.cinema.base.domain.Result.Success;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.randomCreateShow;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowId;
import static org.assertj.core.api.Assertions.assertThat;

class ShowCreatorTest {

    private static final BigDecimal EXPECTED_INITIAL_PRICE = BigDecimal.valueOf(100);

    private final Clock clock = Clock.utc();

    @ParameterizedTest
    @ValueSource(ints = {2, 10, 99, 100})
    void shouldCreateShowWithExactlyMaxSeats(int maxSeats) {
        // given
        var createShow = randomCreateShow(randomShowId(), maxSeats);

        // when
        var result = ShowCreator.create(createShow, clock);

        // then
        var showCreated = expectShowCreated(result);
        var seats = showCreated.initialShow().seats().values();
        assertThat(seats).hasSize(maxSeats);
    }

    @ParameterizedTest
    @ValueSource(ints = {101, 102, 200})
    void shouldFailToCreateShowWithTooManySeats(int maxSeats) {
        // given
        var createShow = randomCreateShow(randomShowId(), maxSeats);

        // when
        var result = ShowCreator.create(createShow, clock);

        // then
        var showCommandError = expectShowCommandError(result);
        assertThat(showCommandError).isEqualTo(ShowCommandError.TOO_MANY_SEATS);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void shouldFailToCreateShowWithTooFewSeats(int maxSeats) {
        // given
        var createShow = randomCreateShow(randomShowId(), maxSeats);

        // when
        var result = ShowCreator.create(createShow, clock);

        // then
        var showCommandError = expectShowCommandError(result);
        assertThat(showCommandError).isEqualTo(ShowCommandError.TOO_FEW_SEATS);
    }

    @Test
    void shouldCreateShowWithAllAvailableSeats() {
        // given
        var createShow = randomCreateShow(randomShowId());

        // when
        var result = ShowCreator.create(createShow, clock);

        // then
        var showCreated = expectShowCreated(result);
        var seats = showCreated.initialShow().seats().values();
        assertThat(seats).allMatch(Seat::isAvailable);
    }

    @Test
    void shouldCreateShowWithSeatsWithExpectedInitialPrice() {
        // given
        var createShow = randomCreateShow(randomShowId());

        // when
        var result = ShowCreator.create(createShow, clock);

        // then
        var showCreated = expectShowCreated(result);
        var seats = showCreated.initialShow().seats().values();
        assertThat(seats).allSatisfy(seat -> assertThat(seat.price()).isEqualTo(EXPECTED_INITIAL_PRICE));
    }

    private static ShowCreated expectShowCreated(Result<ShowCommandError, ShowCreated> result) {
        assertThat(result).isInstanceOf(Success.class);
        return ((Success<ShowCommandError, ShowCreated>) result).value();
    }

    private static ShowCommandError expectShowCommandError(Result<ShowCommandError, ShowCreated> result) {
        assertThat(result).isInstanceOf(Failure.class);
        return ((Failure<ShowCommandError, ShowCreated>) result).error();
    }
}