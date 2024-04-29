package io.github.joamik.cinema.reservation.application.projection;

import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import akka.persistence.testkit.query.javadsl.PersistenceTestKitReadJournal;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowService;
import io.github.joamik.cinema.reservation.application.ShowServiceProperties;
import io.github.joamik.cinema.reservation.domain.SeatNumber;
import io.github.joamik.cinema.reservation.infrastructure.InMemoryShowViewRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.github.joamik.cinema.reservation.application.Await.await;
import static io.github.joamik.cinema.reservation.application.ShowEntity.persistenceId;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowId;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class ShowViewProjectionTest {

    private final Config config = PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    private final ActorSystem system = ActorSystem.create("es-cinema", config);

    private final ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));
    private final Clock clock = Clock.utc();
    private final ShowServiceProperties showServiceProperties = new ShowServiceProperties(5_000);
    private final ShowService showService = new ShowService(sharding, clock, showServiceProperties);
    private final ShowViewRepository showViewRepository = new InMemoryShowViewRepository();
    private final PersistenceTestKitReadJournal readJournal = PersistenceQuery.get(system)
            .getReadJournalFor(PersistenceTestKitReadJournal.class, PersistenceTestKitReadJournal.Identifier());
    private final ShowViewProjection showViewProjection =
            new ShowViewProjection(showViewRepository, Adapter.toTyped(system), readJournal, readJournal);

    @AfterEach
    public void cleanUp() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    void shouldGetAvailableShowViewsUsingByPersistenceId() throws ExecutionException, InterruptedException {
        // given
        var showId1 = randomShowId();
        var showId2 = randomShowId();

        await(showService.createShow(showId1, "Matrix", 2));
        await(showService.reserveSeat(showId1, SeatNumber.of(1)));
        await(showService.reserveSeat(showId1, SeatNumber.of(2))); // no more available seat

        await(showService.createShow(showId2, "Snatch", 20));
        await(showService.reserveSeat(showId2, SeatNumber.of(1)));

        // when
        showViewProjection.run(persistenceId(showId1).id());
        showViewProjection.run(persistenceId(showId2).id());

        // then
        Awaitility.await().atMost(10, SECONDS).untilAsserted(() -> {
            List<ShowView> showViews = await(showViewRepository.findAvailable());
            assertThat(showViews).containsOnly(new ShowView(showId2.id().toString(), 19));
        });
    }

    @Test
    void shouldGetAvailableShowViewsUsingByTag() throws ExecutionException, InterruptedException {
        // given
        var showId1 = randomShowId();
        var showId2 = randomShowId();

        await(showService.createShow(showId1, "Matrix", 2));
        await(showService.reserveSeat(showId1, SeatNumber.of(1)));
        await(showService.reserveSeat(showId1, SeatNumber.of(2))); // no more available seat

        await(showService.createShow(showId2, "Snatch", 20));
        await(showService.reserveSeat(showId2, SeatNumber.of(1)));

        // when
        showViewProjection.runByTag();

        // then
        Awaitility.await().atMost(10, SECONDS).untilAsserted(() -> {
            List<ShowView> showViews = await(showViewRepository.findAvailable());
            assertThat(showViews).containsOnly(new ShowView(showId2.id().toString(), 19));
        });
    }
}