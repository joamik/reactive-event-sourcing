package io.github.joamik.cinema.base.domain;

import java.time.Instant;

public interface Clock {

    Instant now();

    static Clock fixed(Instant now) {
        return () -> now;
    }

    static Clock utc() {
        return () -> java.time.Clock.systemUTC().instant();
    }
}
