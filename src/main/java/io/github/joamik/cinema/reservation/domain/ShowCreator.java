package io.github.joamik.cinema.reservation.domain;

import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.base.domain.Result;
import io.github.joamik.cinema.reservation.domain.ShowCommand.CreateShow;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;

import java.math.BigDecimal;

public class ShowCreator {

    private static final BigDecimal INITIAL_PRICE = BigDecimal.valueOf(100);

    public static Result<ShowCommandError, ShowCreated> create(CreateShow createShow, Clock clock) {
        var seats = SeatsCreator.createSeats(INITIAL_PRICE, createShow.maxSeats());
        var initialShow = new InitialShow(createShow.showId(), createShow.title(), seats);
        var showCreated = new ShowCreated(createShow.showId(), clock.now(), initialShow);
        return Result.success(showCreated);
    }
}
