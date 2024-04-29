package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.base.domain.Result;
import io.github.joamik.cinema.reservation.domain.ShowCommand.CreateShow;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;

import java.math.BigDecimal;

import static io.github.joamik.cinema.reservation.domain.ShowCommandError.TOO_FEW_SEATS;
import static io.github.joamik.cinema.reservation.domain.ShowCommandError.TOO_MANY_SEATS;

public class ShowCreator {

    private static final int MIN_SEATS = 2;
    private static final int MAX_SEATS = 100;

    private static final BigDecimal INITIAL_PRICE = BigDecimal.valueOf(100);

    public static Result<ShowCommandError, ShowCreated> create(CreateShow createShow, Clock clock) {
        if (createShow.maxSeats() < MIN_SEATS) {
            return Result.failure(TOO_FEW_SEATS);
        }

        if (createShow.maxSeats() > MAX_SEATS) {
            return Result.failure(TOO_MANY_SEATS);
        }

        var seats = SeatsCreator.createSeats(INITIAL_PRICE, createShow.maxSeats());
        var initialShow = new InitialShow(createShow.showId(), createShow.title(), seats);
        var showCreated = new ShowCreated(createShow.showId(), clock.now(), initialShow);
        return Result.success(showCreated);
    }
}
