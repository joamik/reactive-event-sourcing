package io.github.joamik.cinema.reservation.application;

import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;
import io.github.joamik.cinema.base.controll.Result;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowEntityCommand.ShowCommandEnvelope;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandRejected;
import io.github.joamik.cinema.reservation.domain.Show;
import io.github.joamik.cinema.reservation.domain.ShowCommandError;
import io.github.joamik.cinema.reservation.domain.ShowEvent;
import io.github.joamik.cinema.reservation.domain.ShowId;

import java.util.List;

public class ShowEntity extends EventSourcedBehaviorWithEnforcedReplies<ShowEntityCommand, ShowEvent, Show> {

    private final ShowId showId;
    private final Clock clock;

    public ShowEntity(PersistenceId persistenceId, ShowId showId, Clock clock) {
        super(persistenceId);
        this.showId = showId;
        this.clock = clock;
    }

    @Override
    public Show emptyState() {
        return Show.create(showId);
    }

    @Override
    public EventHandler<Show, ShowEvent> eventHandler() {
        return newEventHandlerBuilder()
                .forStateType(Show.class)
                .onAnyEvent(Show::apply);
    }

    @Override
    public CommandHandlerWithReply<ShowEntityCommand, ShowEvent, Show> commandHandler() {
        return newCommandHandlerWithReplyBuilder().forStateType(Show.class)
                .onCommand(ShowCommandEnvelope.class, this::handlerShowCommand)
                .build();
    }

    private ReplyEffect<ShowEvent, Show> handlerShowCommand(Show show, ShowCommandEnvelope showCommandEnvelope) {
        Result<ShowCommandError, List<ShowEvent>> result = show.process(showCommandEnvelope.command(), clock);
        return switch (result) {
            case Result.Failure<ShowCommandError, List<ShowEvent>> failure -> Effect()
                    .reply(showCommandEnvelope.replyTo(), new CommandRejected(failure.error()));
            case Result.Success<ShowCommandError, List<ShowEvent>> success -> Effect()
                    .persist(success.value())
                    .thenReply(showCommandEnvelope.replyTo(), _ -> new CommandProcessed());
        };
    }
}
