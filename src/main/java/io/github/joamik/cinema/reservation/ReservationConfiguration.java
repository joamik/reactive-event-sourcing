package io.github.joamik.cinema.reservation;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import io.github.joamik.cinema.base.domain.Clock;
import io.github.joamik.cinema.reservation.application.ShowService;
import io.github.joamik.cinema.reservation.application.ShowServiceProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReservationConfiguration {

    @Bean
    public ShowService showService(ClusterSharding clusterSharding, Clock clock,
            @Value("${cinema.reservation.showService.askTimeMilliseconds:500}") long askTimeMilliseconds) {
        return new ShowService(clusterSharding, clock, new ShowServiceProperties(askTimeMilliseconds));
    }
}
