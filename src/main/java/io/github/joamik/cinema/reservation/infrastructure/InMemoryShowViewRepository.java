package io.github.joamik.cinema.reservation.infrastructure;

import akka.Done;
import io.github.joamik.cinema.reservation.application.projection.ShowView;
import io.github.joamik.cinema.reservation.application.projection.ShowViewRepository;
import io.github.joamik.cinema.reservation.domain.ShowId;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class InMemoryShowViewRepository implements ShowViewRepository {

    private final Map<ShowId, ShowView> showViewById = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<List<ShowView>> findAvailable() {
        return completedFuture(showViewById.values().stream()
                .filter(view -> view.availableSeats() > 0)
                .toList());
    }

    @Override
    public CompletionStage<Done> save(ShowId showId, int availableSeats) {
        return supplyAsync(() -> {
            showViewById.putIfAbsent(showId, new ShowView(showId.id().toString(), availableSeats));
            return Done.done(); // todo JM: isn't using Done here leaking akka abstraction?
        });
    }

    @Override
    public CompletionStage<Done> decrementAvailability(ShowId showId) {
        return supplyAsync(() -> {
            showViewById.computeIfPresent(showId, ((_, view) -> new ShowView(view.showId(), view.availableSeats() - 1)));
            return Done.done();
        });
    }

    @Override
    public CompletionStage<Done> incrementAvailability(ShowId showId) {
        return supplyAsync(() -> {
            showViewById.computeIfPresent(showId, ((_, view) -> new ShowView(view.showId(), view.availableSeats() + 1)));
            return Done.done();
        });
    }
}
