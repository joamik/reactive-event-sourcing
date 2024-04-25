package io.github.joamik.cinema.reservation.application;

import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static io.github.joamik.cinema.reservation.application.Await.await;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomSeatNumber;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowId;
import static org.assertj.core.api.Assertions.assertThat;

class ShowServiceTest {

    private static final Config config = PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    private static final ActorSystem system = ActorSystem.create("es-cinema", config);

    private final ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));
    private final Clock clock = Clock.utc();
    private final ShowServiceProperties showServiceProperties = new ShowServiceProperties(5_000);
    private final ShowService showService = new ShowService(sharding, clock, showServiceProperties);

    @AfterAll
    public static void cleanUp() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    void shouldCreateShow() throws ExecutionException, InterruptedException {
        // given
        var showId = randomShowId();

        // when
        var response = await(showService.createShow(showId, "Title", 10));

        // then
        assertThat(response).isInstanceOf(CommandProcessed.class);
    }

    @Test
    void shouldReserveSeat() throws ExecutionException, InterruptedException {
        // given
        var showId = randomShowId();
        await(showService.createShow(showId, "Title", 10));
        var seatNumber = randomSeatNumber();

        // when
        var response = await(showService.reserveSeat(showId, seatNumber));

        // then
        assertThat(response).isInstanceOf(CommandProcessed.class);
    }

    @Test
    void shouldCancelSeatReservation() throws ExecutionException, InterruptedException {
        // given
        var showId = randomShowId();
        await(showService.createShow(showId, "Title", 10));
        var seatNumber = randomSeatNumber();

        // when
        var reservationResponse = await(showService.reserveSeat(showId, seatNumber));

        // then
        assertThat(reservationResponse).isInstanceOf(CommandProcessed.class);

        // when
        var cancellationResponse = await(showService.cancelReservation(showId, seatNumber));

        // then
        assertThat(cancellationResponse).isInstanceOf(CommandProcessed.class);
    }

    @Test
    void shouldFindShowById() throws ExecutionException, InterruptedException {
        // given
        var showId = randomShowId();
        await(showService.createShow(showId, "Title", 10));

        // when
        var show = await(showService.findShowBy(showId));

        // then
        assertThat(show).isNotEmpty();
        assertThat(show.get().id()).isEqualTo(showId);
    }

    @Test
    void shouldReturnEmptyShow() throws ExecutionException, InterruptedException {
        // given
        var showId = randomShowId();

        // when
        var show = await(showService.findShowBy(showId));

        // then
        assertThat(show).isEmpty();
    }
}