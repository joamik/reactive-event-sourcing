package io.github.joamik.cinema.reservation.application.projection;

import akka.actor.typed.ActorSystem;
import akka.persistence.query.Offset;
import akka.projection.Projection;
import akka.projection.ProjectionId;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.javadsl.SourceProvider;
import akka.projection.jdbc.javadsl.JdbcProjection;
import io.github.joamik.cinema.reservation.domain.ShowEvent;

import javax.sql.DataSource;

import java.time.Duration;

import static akka.projection.HandlerRecoveryStrategy.retryAndFail;
import static java.time.Duration.ofSeconds;

public class ShowViewProjection {

    public static final ProjectionId PROJECTION_ID = ProjectionId.of("show-events", "show-view");

    private final ShowViewEventHandler showViewEventHandler;
    private final ActorSystem<?> actorSystem;
    private final DataSource dataSource;

    private final int saveOffsetAfterEnvelopes = 100;
    private final Duration saveOffsetAfterDuration = Duration.ofMillis(500);

    public ShowViewProjection(
            ShowViewEventHandler showViewEventHandler,
            ActorSystem<?> actorSystem,
            DataSource dataSource) {
        this.showViewEventHandler = showViewEventHandler;
        this.actorSystem = actorSystem;
        this.dataSource = dataSource;
    }

    public Projection<EventEnvelope<ShowEvent>> create(SourceProvider<Offset, EventEnvelope<ShowEvent>> sourceProvider) {
        return JdbcProjection.atLeastOnceAsync(
                        PROJECTION_ID,
                        sourceProvider,
                        () -> new DataSourceJdbcSession(dataSource),
                        () -> showViewEventHandler,
                        actorSystem)
                .withSaveOffset(saveOffsetAfterEnvelopes, saveOffsetAfterDuration)
                .withRecoveryStrategy(retryAndFail(4, ofSeconds(5)))
                .withRestartBackoff(ofSeconds(3), ofSeconds(30), 0.1d);
    }
}
