package io.github.joamik.cinema.reservation.api;

import io.github.joamik.cinema.reservation.application.ShowService;
import io.github.joamik.cinema.reservation.domain.Show;
import io.github.joamik.cinema.reservation.domain.ShowId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping(value = "/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping(value = "{showId}", produces = "application/json")
    public Mono<ShowResponse> findById(@PathVariable UUID showId) {
        CompletionStage<Show> show = showService.findShowBy(ShowId.of(showId));
        CompletionStage<ShowResponse> showResponse = show.thenApply(ShowResponse::from);
        return Mono.fromCompletionStage(showResponse);
    }

    // POST /shows/{id}/seats/{id} body: {"action": "cancel_reservation"} / {"action": "reserve"}
}
