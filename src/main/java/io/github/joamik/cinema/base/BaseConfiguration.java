package io.github.joamik.cinema.base;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.joamik.cinema.base.application.VoidBehavior;
import io.github.joamik.cinema.base.domain.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseConfiguration {

    @Bean
    public Clock clock() {
        return Clock.utc();
    }

    @Bean
    public Config config() {
        // todo JM: temporary usage of PersistenceTestKitPlugin
        return PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    }

    @Bean(destroyMethod = "terminate")
    public ActorSystem<Void> actorSystem(Config config) {
        return ActorSystem.create(VoidBehavior.create(), "es-cinema", config);
    }

    @Bean
    public ClusterSharding clusterSharding(ActorSystem<?> actorSystem) {
        return ClusterSharding.get(actorSystem);
    }
}
