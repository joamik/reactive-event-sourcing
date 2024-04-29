package io.github.joamik.cinema.reservation.application.projection;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.javadsl.EventsByPersistenceIdQuery;
import io.github.joamik.cinema.reservation.domain.ShowEvent;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReservationCancelled;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;

import java.util.concurrent.CompletionStage;

public class ShowViewProjection {

    private final ShowViewRepository showViewRepository;
    private final ActorSystem<?> actorSystem;
    private final EventsByPersistenceIdQuery byPersistenceIdQuery;

    public ShowViewProjection(
            ShowViewRepository showViewRepository,
            ActorSystem<?> actorSystem,
            EventsByPersistenceIdQuery byPersistenceIdQuery) {
        this.showViewRepository = showViewRepository;
        this.actorSystem = actorSystem;
        this.byPersistenceIdQuery = byPersistenceIdQuery;
    }

    public CompletionStage<Done> run(String persistenceId) {
        long from = 0;
        long to = Long.MAX_VALUE;
        return byPersistenceIdQuery.eventsByPersistenceId(persistenceId, from, to)
                .mapAsync(1, this::processEvent)
                .run(actorSystem);
    }

    private CompletionStage<Done> processEvent(EventEnvelope eventEnvelope) {
        if (eventEnvelope.event() instanceof ShowEvent showEvent) {
            switch (showEvent) {
                case ShowCreated showCreated -> {
                    return showViewRepository.save(showCreated.showId(), showCreated.initialShow().seats().size());
                }
                case SeatReserved seatReserved -> {
                    return showViewRepository.decrementAvailability(seatReserved.showId());
                }
                case SeatReservationCancelled seatReservationCancelled -> {
                    return showViewRepository.incrementAvailability(seatReservationCancelled.showId());
                }
            }
        } else {
            throw new IllegalStateException("Unrecognized event type");
        }
    }
}
