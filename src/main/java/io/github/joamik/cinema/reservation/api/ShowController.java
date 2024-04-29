package io.github.joamik.cinema.reservation.api;

import io.github.joamik.cinema.reservation.application.ShowEntityResponse;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandRejected;
import io.github.joamik.cinema.reservation.application.ShowService;
import io.github.joamik.cinema.reservation.domain.SeatNumber;
import io.github.joamik.cinema.reservation.domain.ShowCommandError;
import io.github.joamik.cinema.reservation.domain.ShowId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> create(@RequestBody CreateShowRequest request) {
        CompletionStage<ResponseEntity<String>> showResponse = showService.createShow(ShowId.of(request.id()), request.title(), request.maxSeats())
                .thenApply(response -> switch (response) {
                    case CommandProcessed _ -> ResponseEntity.created(toShowLocation(request.id())).build();
                    case CommandRejected commandRejected -> {
                        if (commandRejected.error() == ShowCommandError.SHOW_ALREADY_EXISTS) {
                            yield new ResponseEntity<>("Show already created", CONFLICT);
                        } else {
                            yield badRequest().body(STR."Show creation failed with: \{commandRejected.error().name()}");
                        }
                    }
                });

        return Mono.fromCompletionStage(showResponse);
    }

    @GetMapping(value = "{showId}", produces = "application/json")
    public Mono<ResponseEntity<ShowResponse>> findById(@PathVariable UUID showId) {
        CompletionStage<ResponseEntity<ShowResponse>> showResponse = showService.findShowBy(ShowId.of(showId))
                .thenApply(show -> show.map(ShowResponse::from)
                        .map(ok()::body)
                        .orElse(notFound().build()));

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
            case CommandRejected commandRejected -> badRequest()
                    .body(new ReserveResponse(STR."\{request.action()} failed with: \{commandRejected.error()}"));
        });

        return Mono.fromCompletionStage(reserveResponse);
    }

    private static URI toShowLocation(UUID showId) {
        try {
            return new URI(STR."/shows/\{showId}");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
