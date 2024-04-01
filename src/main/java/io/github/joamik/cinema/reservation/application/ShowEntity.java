package io.github.joamik.cinema.reservation.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;
import io.github.joamik.cinema.base.controll.Result;
import io.github.joamik.cinema.base.controll.Result.Failure;
import io.github.joamik.cinema.base.controll.Result.Success;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowEntityCommand.GetShow;
import io.github.joamik.cinema.reservation.application.ShowEntityCommand.ShowCommandEnvelope;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandRejected;
import io.github.joamik.cinema.reservation.domain.Show;
import io.github.joamik.cinema.reservation.domain.ShowCommandError;
import io.github.joamik.cinema.reservation.domain.ShowEvent;
import io.github.joamik.cinema.reservation.domain.ShowId;

import java.util.List;

public class ShowEntity extends EventSourcedBehaviorWithEnforcedReplies<ShowEntityCommand, ShowEvent, Show> {

    public static final EntityTypeKey<ShowEntityCommand> SHOW_ENTITY_TYPE_KEY =
            EntityTypeKey.create(ShowEntityCommand.class, "Show");

    private final ShowId showId;
    private final Clock clock;
    private final ActorContext<ShowEntityCommand> context;

    private ShowEntity(PersistenceId persistenceId, ShowId showId, Clock clock, ActorContext<ShowEntityCommand> context) {
        super(persistenceId);
        this.showId = showId;
        this.clock = clock;
        this.context = context;
    }

    public static Behavior<ShowEntityCommand> create(ShowId showId, Clock clock) {
        return Behaviors.setup(context -> {
            var persistenceId = PersistenceId.of("Show", showId.id().toString());
            context.getLog().info("ShowEntity {} initialization started", showId);
            return new ShowEntity(persistenceId, showId, clock, context);
        });
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
                .onCommand(GetShow.class, this::returnState)
                .onCommand(ShowCommandEnvelope.class, this::handleShowCommand)
                .build();
    }

    private ReplyEffect<ShowEvent, Show> returnState(Show show, GetShow getShow) {
        return Effect().reply(getShow.replyTo(), show);
    }

    private ReplyEffect<ShowEvent, Show> handleShowCommand(Show show, ShowCommandEnvelope showCommandEnvelope) {
        Result<ShowCommandError, List<ShowEvent>> result = show.process(showCommandEnvelope.command(), clock);
        return switch (result) {
            case Failure<ShowCommandError, List<ShowEvent>> failure -> Effect()
                    .reply(showCommandEnvelope.replyTo(), new CommandRejected(failure.error()));
            case Success<ShowCommandError, List<ShowEvent>> success -> Effect()
                    .persist(success.value())
                    .thenReply(showCommandEnvelope.replyTo(), _ -> new CommandProcessed());
        };
    }
}
