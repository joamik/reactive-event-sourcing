package io.github.joamik.cinema.reservation.domain;

import java.util.UUID;

public class ShowFixture {

    public static Show randomShow() {
        return Show.create(randomShowId());
    }

    public static ShowId randomShowId() {
        return new ShowId(UUID.randomUUID());
    }
}
