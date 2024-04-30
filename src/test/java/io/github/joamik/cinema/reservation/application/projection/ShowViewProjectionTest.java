package io.github.joamik.cinema.reservation.application.projection;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.joamik.cinema.base.application.SpawningBehavior;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.ReservationConfiguration;
import io.github.joamik.cinema.reservation.application.ShowService;
import io.github.joamik.cinema.reservation.domain.SeatNumber;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.github.joamik.cinema.reservation.application.Await.await;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowId;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled(value = "Before enabling, run: docker-compose -p cinema -f development/docker-compose-jdbc.yml up")
class ShowViewProjectionTest {

    private static final Config config = ConfigFactory.load();
    private static final ActorSystem<SpawnProtocol.Command> system = ActorSystem.create(SpawningBehavior.create(), "es-cinema", config);

    private final ClusterSharding sharding = ClusterSharding.get(system);
    private final Clock clock = Clock.utc();

    private ReservationConfiguration reservationConfiguration = new ReservationConfiguration(system, sharding, clock);
    private ShowService showService = reservationConfiguration.showService(5_000);
    private ShowViewRepository showViewRepository = reservationConfiguration.showViewRepository();
    private ProjectionLauncher projectionLauncher = reservationConfiguration.projectionLauncher(showViewRepository);

    @AfterEach
    public void cleanUp() {
        TestKit.shutdownActorSystem(Adapter.toClassic(system));
    }

    @Test
    void shouldGetAvailableShowViewsUsingByPersistenceId() throws ExecutionException, InterruptedException {
        // given
        var showId1 = randomShowId();
        var showId2 = randomShowId();

        await(showService.createShow(showId1, "Matrix", 2));
        await(showService.reserveSeat(showId1, SeatNumber.of(1)));
        await(showService.reserveSeat(showId1, SeatNumber.of(2))); // no more available seat

        await(showService.createShow(showId2, "Snatch", 2));
        await(showService.reserveSeat(showId2, SeatNumber.of(1)));

        // when
        projectionLauncher.runProjections();

        // then
        try {
            Awaitility.await().atMost(10, SECONDS).untilAsserted(() -> {
                List<ShowView> showViews = await(showViewRepository.findAvailable());
                assertThat(showViews).containsOnly(new ShowView(showId2.id().toString(), 1));
            });
        } finally {
            projectionLauncher.shutdownProjections();
        }
    }
}