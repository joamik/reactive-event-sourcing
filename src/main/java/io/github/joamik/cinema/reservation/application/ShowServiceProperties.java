package io.github.joamik.cinema.reservation.application;

import java.time.Duration;

public class ShowServiceProperties {

    private final Duration askTimeout;

    public ShowServiceProperties(long askTimeoutMilliseconds) {
        this.askTimeout = Duration.ofMillis(askTimeoutMilliseconds);
    }

    public Duration getAskTimeout() {
        return askTimeout;
    }
}
