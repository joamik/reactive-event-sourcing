package io.github.joamik.cinema.reservation.application;

import io.github.joamik.cinema.reservation.domain.ShowCommandError;

import java.io.Serializable;

public sealed interface ShowEntityResponse extends Serializable {

    record CommandProcessed() implements ShowEntityResponse {

    }

    record CommandRejected(ShowCommandError error) implements ShowEntityResponse {

    }
}
