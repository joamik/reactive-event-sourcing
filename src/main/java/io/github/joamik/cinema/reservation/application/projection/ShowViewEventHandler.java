package io.github.joamik.cinema.reservation.application.projection;

import akka.Done;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.javadsl.Handler;
import io.github.joamik.cinema.reservation.domain.ShowEvent;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReservationCancelled;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;

import java.util.concurrent.CompletionStage;

public class ShowViewEventHandler extends Handler<EventEnvelope<ShowEvent>> {

    private final ShowViewRepository showViewRepository;

    public ShowViewEventHandler(ShowViewRepository showViewRepository) {
        this.showViewRepository = showViewRepository;
    }

    @Override
    public CompletionStage<Done> process(EventEnvelope<ShowEvent> showEventEventEnvelope) throws Exception {
        return switch (showEventEventEnvelope.event()) {
            case ShowCreated showCreated ->
                    showViewRepository.save(showCreated.showId(), showCreated.initialShow().seats().size());
            case SeatReserved seatReserved ->
                    showViewRepository.decrementAvailability(seatReserved.showId());
            case SeatReservationCancelled seatReservationCancelled ->
                    showViewRepository.incrementAvailability(seatReservationCancelled.showId());
        };
    }
}
