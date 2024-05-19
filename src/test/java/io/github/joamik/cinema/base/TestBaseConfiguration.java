package io.github.joamik.cinema.base;

import akka.persistence.testkit.PersistenceTestKitPlugin;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestBaseConfiguration {

    @Bean
    @Primary
    public Config testConfig() {
        return PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    }
}
