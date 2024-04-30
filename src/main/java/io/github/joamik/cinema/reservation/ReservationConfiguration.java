package io.github.joamik.cinema.reservation;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SpawnProtocol;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.Offset;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.SourceProvider;
import com.zaxxer.hikari.HikariDataSource;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowEntity;
import io.github.joamik.cinema.reservation.application.ShowService;
import io.github.joamik.cinema.reservation.application.ShowServiceProperties;
import io.github.joamik.cinema.reservation.application.projection.ProjectionLauncher;
import io.github.joamik.cinema.reservation.application.projection.ShowViewEventHandler;
import io.github.joamik.cinema.reservation.application.projection.ShowViewProjection;
import io.github.joamik.cinema.reservation.application.projection.ShowViewRepository;
import io.github.joamik.cinema.reservation.domain.ShowEvent;
import io.github.joamik.cinema.reservation.infrastructure.InMemoryShowViewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ReservationConfiguration {

    private final ActorSystem<SpawnProtocol.Command> actorSystem;
    private final ClusterSharding clusterSharding;
    private final Clock clock;

    public ReservationConfiguration(ActorSystem<SpawnProtocol.Command> actorSystem, ClusterSharding clusterSharding, Clock clock) {
        this.actorSystem = actorSystem;
        this.clusterSharding = clusterSharding;
        this.clock = clock;
    }

    @Bean
    public ShowService showService(
            @Value("${cinema.reservation.showService.askTimeMilliseconds:500}") long askTimeMilliseconds) {
        return new ShowService(clusterSharding, clock, new ShowServiceProperties(askTimeMilliseconds));
    }

    @Bean
    public ShowViewRepository showViewRepository() {
        return new InMemoryShowViewRepository();
    }

    @SuppressWarnings("unchecked")
    @Bean(initMethod = "runProjections")
    public ProjectionLauncher projectionLauncher(ShowViewRepository showViewRepository) {
        var showViewEventHandler = new ShowViewEventHandler(showViewRepository);
        var showViewProjection = new ShowViewProjection(showViewEventHandler, actorSystem, dataSource());
        var projection = showViewProjection.create(showEventsSourceProvider());
        var projectionLauncher = new ProjectionLauncher(actorSystem);
        projectionLauncher.withLocalProjections(projection);
        return projectionLauncher;
    }

    private SourceProvider<Offset, EventEnvelope<ShowEvent>> showEventsSourceProvider() {
        return EventSourcedProvider.eventsByTag(actorSystem, JdbcReadJournal.Identifier(), ShowEntity.SHOW_EVENT_TAG);
    }

    private DataSource dataSource() {
        var hikariDataSource = new HikariDataSource();
        hikariDataSource.setPoolName("projection-data-source");
        hikariDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        hikariDataSource.setUsername("admin");
        hikariDataSource.setPassword("admin");
        hikariDataSource.setMaximumPoolSize(5);
        hikariDataSource.setRegisterMbeans(true);
        return hikariDataSource;
    }
}
