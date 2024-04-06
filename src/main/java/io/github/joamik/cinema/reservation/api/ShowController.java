package io.github.joamik.cinema.reservation.api;

import io.github.joamik.cinema.reservation.application.ShowEntityResponse;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandRejected;
import io.github.joamik.cinema.reservation.application.ShowService;
import io.github.joamik.cinema.reservation.domain.SeatNumber;
import io.github.joamik.cinema.reservation.domain.Show;
import io.github.joamik.cinema.reservation.domain.ShowId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PatchMapping(value = "{showId}/seats/{seatNumber}", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<ReserveResponse>> reserve(@PathVariable UUID showId, @PathVariable int seatNumber, @RequestBody SeatActionRequest request) {
        CompletionStage<ShowEntityResponse> showEntityResponse = switch (request.action()) {
            case RESERVE -> showService.reserveSeat(ShowId.of(showId), SeatNumber.of(seatNumber));
            case CANCEL_RESERVATION -> showService.cancelReservation(ShowId.of(showId), SeatNumber.of(seatNumber));
        };

        CompletionStage<ResponseEntity<ReserveResponse>> reserveResponse = showEntityResponse.thenApply(response -> switch (response) {
            case CommandProcessed _ -> ResponseEntity.accepted()
                    .body(new ReserveResponse(STR."\{request.action()} successful"));
            case CommandRejected commandRejected -> ResponseEntity.badRequest()
                    .body(new ReserveResponse(STR."\{request.action()} failed with: \{commandRejected.error()}"));
        });

        return Mono.fromCompletionStage(reserveResponse);
    }
}
