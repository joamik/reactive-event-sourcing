package io.github.joamik.cinema.reservation.application.projection;

import akka.Done;
import io.github.joamik.cinema.reservation.domain.ShowId;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface ShowViewRepository {

    CompletionStage<List<ShowView>> findAvailable();

    CompletionStage<Done> save(ShowId showId, int availableSeats);

    CompletionStage<Done> decrementAvailability(ShowId showId);

    CompletionStage<Done> incrementAvailability(ShowId showId);
}
