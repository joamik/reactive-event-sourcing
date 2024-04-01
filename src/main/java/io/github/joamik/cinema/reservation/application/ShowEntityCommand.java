package io.github.joamik.cinema.reservation.application;

import akka.actor.typed.ActorRef;
import io.github.joamik.cinema.reservation.domain.ShowCommand;

import java.io.Serializable;

public sealed interface ShowEntityCommand extends Serializable {

    record ShowCommandEnvelope(ShowCommand command, ActorRef<ShowEntityResponse> replyTo) implements ShowEntityCommand {

    }
}
