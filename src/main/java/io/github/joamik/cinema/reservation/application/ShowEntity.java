package io.github.joamik.cinema.reservation.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;
import io.github.joamik.cinema.base.domain.Result;
import io.github.joamik.cinema.base.domain.Result.Failure;
import io.github.joamik.cinema.base.domain.Result.Success;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowEntityCommand.GetShow;
import io.github.joamik.cinema.reservation.application.ShowEntityCommand.ShowCommandEnvelope;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandRejected;
import io.github.joamik.cinema.reservation.domain.Show;
import io.github.joamik.cinema.reservation.domain.ShowCommand;
import io.github.joamik.cinema.reservation.domain.ShowCommand.CreateShow;
import io.github.joamik.cinema.reservation.domain.ShowCommandError;
import io.github.joamik.cinema.reservation.domain.ShowCreator;
import io.github.joamik.cinema.reservation.domain.ShowEvent;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;
import io.github.joamik.cinema.reservation.domain.ShowId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ShowEntity extends EventSourcedBehaviorWithEnforcedReplies<ShowEntityCommand, ShowEvent, Show> {

    public static final EntityTypeKey<ShowEntityCommand> SHOW_ENTITY_TYPE_KEY =
            EntityTypeKey.create(ShowEntityCommand.class, "Show");

    public static final String SHOW_EVENT_TAG = "ShowEvent";

    private final ShowId showId;
    private final Clock clock;
    private final ActorContext<ShowEntityCommand> context;

    private ShowEntity(PersistenceId persistenceId, ShowId showId, Clock clock, ActorContext<ShowEntityCommand> context) {
        super(persistenceId);
        this.showId = showId;
        this.clock = clock;
        this.context = context;
    }

    public static PersistenceId persistenceId(ShowId showId) {
        return PersistenceId.of(SHOW_ENTITY_TYPE_KEY.name(), showId.id().toString());
    }

    public static Behavior<ShowEntityCommand> create(ShowId showId, Clock clock) {
        return Behaviors.setup(context -> {
            var persistenceId = ShowEntity.persistenceId(showId);
            context.getLog().info("ShowEntity {} initialization started", showId);
            return new ShowEntity(persistenceId, showId, clock, context);
        });
    }

    @Override
    public Show emptyState() {
        return null;
    }

    @Override
    public EventHandler<Show, ShowEvent> eventHandler() {
        EventHandlerBuilder<Show, ShowEvent> builder = newEventHandlerBuilder();

        builder.forNullState()
                .onEvent(ShowCreated.class, Show::create);

        builder.forStateType(Show.class)
                .onAnyEvent(Show::apply);

        return builder.build();

    }

    @Override
    public CommandHandlerWithReply<ShowEntityCommand, ShowEvent, Show> commandHandler() {
        CommandHandlerWithReplyBuilder<ShowEntityCommand, ShowEvent, Show> builder = newCommandHandlerWithReplyBuilder();

        builder.forNullState()
                .onCommand(GetShow.class, this::returnEmptyState)
                .onCommand(ShowCommandEnvelope.class, this::handleShowCreation)
                .build();

        builder.forStateType(Show.class)
                .onCommand(GetShow.class, this::returnState)
                .onCommand(ShowCommandEnvelope.class, this::handleShowCommand)
                .build();

        return builder.build();
    }

    @Override
    public Set<String> tagsFor(ShowEvent showEvent) {
        return Set.of(SHOW_EVENT_TAG);
    }

    private ReplyEffect<ShowEvent, Show> returnEmptyState(GetShow getShow) {
        return Effect().reply(getShow.replyTo(), Optional.empty());
    }

    private ReplyEffect<ShowEvent, Show> handleShowCreation(ShowCommandEnvelope envelope) {
        ShowCommand command = envelope.command();
        if (command instanceof CreateShow createShow) {
            Result<ShowCommandError, ShowCreated> result = ShowCreator.create(createShow, clock);
            return switch (result) {
                case Failure<ShowCommandError, ShowCreated> failure -> Effect()
                        .reply(envelope.replyTo(), new CommandRejected(failure.error()));
                case Success<ShowCommandError, ShowCreated> success -> Effect()
                        .persist(success.value())
                        .thenReply(envelope.replyTo(), _ -> new CommandProcessed());
            };
        } else {
            context.getLog().warn("Show {} not created", command.showId());
            return Effect().reply(envelope.replyTo(), new CommandRejected(ShowCommandError.SHOW_NOT_EXISTS));
        }
    }

    private ReplyEffect<ShowEvent, Show> returnState(Show show, GetShow getShow) {
        return Effect().reply(getShow.replyTo(), Optional.of(show));
    }

    private ReplyEffect<ShowEvent, Show> handleShowCommand(Show show, ShowCommandEnvelope envelope) {
        Result<ShowCommandError, List<ShowEvent>> result = show.process(envelope.command(), clock);
        return switch (result) {
            case Failure<ShowCommandError, List<ShowEvent>> failure -> Effect()
                    .reply(envelope.replyTo(), new CommandRejected(failure.error()));
            case Success<ShowCommandError, List<ShowEvent>> success -> Effect()
                    .persist(success.value())
                    .thenReply(envelope.replyTo(), _ -> new CommandProcessed());
        };
    }
}
