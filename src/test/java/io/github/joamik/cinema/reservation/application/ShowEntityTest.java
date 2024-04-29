package io.github.joamik.cinema.reservation.application;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import akka.persistence.testkit.javadsl.EventSourcedBehaviorTestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowEntityCommand.ShowCommandEnvelope;
import io.github.joamik.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import io.github.joamik.cinema.reservation.domain.Show;
import io.github.joamik.cinema.reservation.domain.ShowCommand;
import io.github.joamik.cinema.reservation.domain.ShowCommandFixture;
import io.github.joamik.cinema.reservation.domain.ShowEvent;
import io.github.joamik.cinema.reservation.domain.ShowEvent.SeatReserved;
import io.github.joamik.cinema.reservation.domain.ShowEvent.ShowCreated;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.randomCreateShow;
import static io.github.joamik.cinema.reservation.domain.ShowCommandFixture.reserveRandomSeat;
import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowId;
import static org.assertj.core.api.Assertions.assertThat;

class ShowEntityTest {

    private static final Config UNIT_TEST_AKKA_CONFIGURATION = ConfigFactory.parseString("""
                akka.actor.enable-additional-serialization-bindings=on
                akka.actor.allow-java-serialization=on
                akka.actor.warn-about-java-serializer-usage=off
                akka.loglevel=INFO
            """);

    private static final ActorTestKit testKit = ActorTestKit.create(EventSourcedBehaviorTestKit.config()
            .withFallback(UNIT_TEST_AKKA_CONFIGURATION));

    private final Clock clock = Clock.fixed(Instant.parse("2024-03-16T21:32:05Z"));

    @AfterAll
    public static void cleanUp() {
        testKit.shutdownTestKit();
    }

    @Test
    void shouldCreateShow() {
        // given
        var showId = randomShowId();
        var showEntityKit = EventSourcedBehaviorTestKit.<ShowEntityCommand, ShowEvent, Show>create(testKit.system(), ShowEntity.create(showId, clock));
        var createShow = randomCreateShow(showId);

        // when
        var result = showEntityKit.<ShowEntityResponse>runCommand(replyTo -> toEnvelope(createShow, replyTo));

        // then
        assertThat(result.reply()).isInstanceOf(CommandProcessed.class);
        assertThat(result.event()).isInstanceOf(ShowCreated.class);
        var createdShow = result.state();
        assertThat(createdShow.id()).isEqualTo(showId);
    }

    @Test
    void shouldCreateShow_withProbe() {
        // given
        var showId = randomShowId();
        var showEntityRef = testKit.spawn(ShowEntity.create(showId, clock));
        var commandResponseProbe = testKit.<ShowEntityResponse>createTestProbe();
        var showResponseProbe = testKit.<Optional<Show>>createTestProbe();
        var createShow = randomCreateShow(showId);

        // when
        showEntityRef.tell(toEnvelope(createShow, commandResponseProbe.ref()));

        // then
        commandResponseProbe.expectMessageClass(CommandProcessed.class);

        // when
        showEntityRef.tell(new ShowEntityCommand.GetShow(showResponseProbe.ref()));

        // then
        var returnedShow = showResponseProbe.receiveMessage();
        assertThat(returnedShow).isNotEmpty();
        assertThat(returnedShow.get().id()).isEqualTo(showId);
    }

    @Test
    void shouldReserveSeat() {
        // given
        var showId = randomShowId();
        var showEntityKit = EventSourcedBehaviorTestKit.<ShowEntityCommand, ShowEvent, Show>create(testKit.system(), ShowEntity.create(showId, clock));
        var createShow = randomCreateShow(showId);
        var reserveSeat = reserveRandomSeat(showId);

        showEntityKit.<ShowEntityResponse>runCommand(replyTo -> toEnvelope(createShow, replyTo));

        // when
        var result = showEntityKit.<ShowEntityResponse>runCommand(replyTo -> toEnvelope(reserveSeat, replyTo));

        // then
        assertThat(result.reply()).isInstanceOf(CommandProcessed.class);
        assertThat(result.event()).isInstanceOf(SeatReserved.class);
        var reservedSeat = result.state().seats().get(reserveSeat.seatNumber());
        assertThat(reservedSeat.isReserved()).isTrue();
    }

    @Test
    void shouldReserveSeat_withProbe() {
        // given
        var showId = randomShowId();
        var showEntityRef = testKit.spawn(ShowEntity.create(showId, clock));
        var commandResponseProbe = testKit.<ShowEntityResponse>createTestProbe();
        var showResponseProbe = testKit.<Optional<Show>>createTestProbe();
        var createShow = randomCreateShow(showId);
        var reserveSeat = reserveRandomSeat(showId);

        // when
        showEntityRef.tell(toEnvelope(createShow, commandResponseProbe.ref()));

        // then
        commandResponseProbe.expectMessageClass(CommandProcessed.class);

        // when
        showEntityRef.tell(toEnvelope(reserveSeat, commandResponseProbe.ref()));

        // then
        commandResponseProbe.expectMessageClass(CommandProcessed.class);

        // when
        showEntityRef.tell(new ShowEntityCommand.GetShow(showResponseProbe.ref()));

        // then
        var returnedShow = showResponseProbe.receiveMessage().orElseThrow();
        var reservedSeat = returnedShow.seats().get(reserveSeat.seatNumber());
        assertThat(reservedSeat.isReserved()).isTrue();
    }

    @Test
    void shouldReturnEmptyShow_withProbe() {
        // given
        var showId = randomShowId();
        var showEntityRef = testKit.spawn(ShowEntity.create(showId, clock));
        var showResponseProbe = testKit.<Optional<Show>>createTestProbe();

        // when
        showEntityRef.tell(new ShowEntityCommand.GetShow(showResponseProbe.ref()));

        // then
        var returnedShow = showResponseProbe.receiveMessage();
        assertThat(returnedShow).isEmpty();
    }

    private ShowEntityCommand toEnvelope(ShowCommand command, ActorRef<ShowEntityResponse> replyTo) {
        return new ShowCommandEnvelope(command, replyTo);
    }
}