package io.github.joamik.cinema.reservation.domain;

import java.io.Serializable;
import java.util.UUID;

public record ShowId(UUID id) implements Serializable {

    public static ShowId of(UUID id) {
        return new ShowId(id);
    }
}
