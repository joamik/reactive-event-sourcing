package io.github.joamik.cinema.reservation.application;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowEntityCommand.GetShow;
import io.github.joamik.cinema.reservation.application.ShowEntityCommand.ShowCommandEnvelope;
import io.github.joamik.cinema.reservation.domain.SeatNumber;
import io.github.joamik.cinema.reservation.domain.Show;
import io.github.joamik.cinema.reservation.domain.ShowCommand;
import io.github.joamik.cinema.reservation.domain.ShowCommand.CancelSeatReservation;
import io.github.joamik.cinema.reservation.domain.ShowCommand.ReserveSeat;
import io.github.joamik.cinema.reservation.domain.ShowId;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class ShowService {

    private final ClusterSharding sharding;
    private final ShowServiceProperties properties;

    public ShowService(ClusterSharding sharding, Clock clock, ShowServiceProperties properties) {
        this.sharding = sharding;
        this.properties = properties;
        sharding.init(Entity.of(ShowEntity.SHOW_ENTITY_TYPE_KEY, entityContext -> {
            var showId = new ShowId(UUID.fromString(entityContext.getEntityId()));
            return ShowEntity.create(showId, clock);
        }));
    }

    public CompletionStage<Show> findShowBy(ShowId showId) {
        return getShowEntityRef(showId).ask(GetShow::new, properties.getAskTimeout());
    }

    public CompletionStage<ShowEntityResponse> reserveSeat(ShowId showId, SeatNumber seatNumber) {
        return askCommand(new ReserveSeat(showId, seatNumber));
    }

    public CompletionStage<ShowEntityResponse> cancelReservation(ShowId showId, SeatNumber seatNumber) {
        return askCommand(new CancelSeatReservation(showId, seatNumber));
    }

    private CompletionStage<ShowEntityResponse> askCommand(ShowCommand showCommand) {
        return getShowEntityRef(showCommand.showId())
                .ask(replyTo -> new ShowCommandEnvelope(showCommand, replyTo), properties.getAskTimeout());
    }

    private EntityRef<ShowEntityCommand> getShowEntityRef(ShowId showId) {
        return sharding.entityRefFor(ShowEntity.SHOW_ENTITY_TYPE_KEY, showId.id().toString());
    }
}
