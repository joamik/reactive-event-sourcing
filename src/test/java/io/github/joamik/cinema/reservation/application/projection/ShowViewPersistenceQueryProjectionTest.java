package io.github.joamik.cinema.reservation.application.projection;

import akka.Done;
import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.Offset;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import akka.persistence.testkit.query.javadsl.PersistenceTestKitReadJournal;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowEntity;
import io.github.joamik.cinema.reservation.application.ShowService;
import io.github.joamik.cinema.reservation.application.ShowServiceProperties;
import io.github.joamik.cinema.reservation.domain.SeatNumber;
import io.github.joamik.cinema.reservation.domain.ShowEvent;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReservationCancelled;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;
import io.github.joamik.cinema.reservation.infrastructure.InMemoryShowViewRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static io.github.joamik.cinema.reservation.application.Await.await;
import static io.github.joamik.cinema.reservation.application.ShowEntity.persistenceId;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowId;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class ShowViewPersistenceQueryProjectionTest {

    private static final Config config = PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    private static final ActorSystem system = ActorSystem.create("es-cinema", config);

    private final ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));
    private final Clock clock = Clock.utc();
    private final ShowServiceProperties showServiceProperties = new ShowServiceProperties(5_000);
    private final ShowService showService = new ShowService(sharding, clock, showServiceProperties);
    private final ShowViewRepository showViewRepository = new InMemoryShowViewRepository();
    private final PersistenceTestKitReadJournal readJournal = PersistenceQuery.get(system)
            .getReadJournalFor(PersistenceTestKitReadJournal.class, PersistenceTestKitReadJournal.Identifier());

    @AfterAll
    public static void cleanUp() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void shouldGetAvailableShowViewsUsingByPersistenceId() throws ExecutionException, InterruptedException {
        //given
        var showId1 = randomShowId();
        var showId2 = randomShowId();

        await(showService.createShow(showId1, "Matrix", 2));
        await(showService.reserveSeat(showId1, SeatNumber.of(1)));
        await(showService.reserveSeat(showId1, SeatNumber.of(2))); // no more available seat

        await(showService.createShow(showId2, "Snatch", 20));
        await(showService.reserveSeat(showId2, SeatNumber.of(1)));

        //when
        readJournal.eventsByPersistenceId(persistenceId(showId1).id(), 0, Long.MAX_VALUE)
                .mapAsync(1, this::processEvent)
                .run(system);

        readJournal.eventsByPersistenceId(persistenceId(showId2).id(), 0, Long.MAX_VALUE)
                .mapAsync(1, this::processEvent)
                .run(system);

        //then
        Awaitility.await().atMost(10, SECONDS).untilAsserted(() -> {
            List<ShowView> showViews = await(showViewRepository.findAvailable());
            assertThat(showViews).contains(new ShowView(showId2.id().toString(), 19));
        });
    }

    @Test
    public void shouldGetAvailableShowViewsUsingByTag() throws ExecutionException, InterruptedException {
        //given
        var showId1 = randomShowId();
        var showId2 = randomShowId();

        await(showService.createShow(showId1, "Matrix", 2));
        await(showService.reserveSeat(showId1, SeatNumber.of(1)));
        await(showService.reserveSeat(showId1, SeatNumber.of(2))); // no more available seat

        await(showService.createShow(showId2, "Snatch", 20));
        await(showService.reserveSeat(showId2, SeatNumber.of(1)));

        //when
        readJournal.currentEventsByTag(ShowEntity.SHOW_EVENT_TAG, Offset.noOffset())
                .mapAsync(1, this::processEvent)
                .run(system);

        //then
        Awaitility.await().atMost(10, SECONDS).untilAsserted(() -> {
            List<ShowView> showViews = await(showViewRepository.findAvailable());
            assertThat(showViews).contains(new ShowView(showId2.id().toString(), 19));
        });
    }

    // todo JM: test ShowViewProjection instead of copying code
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