package io.github.joamik.cinema.reservation.application;

import akka.actor.typed.ActorRef;
import io.github.joamik.cinema.reservation.domain.Show;
import io.github.joamik.cinema.reservation.domain.ShowCommand;

import java.io.Serializable;
import java.util.Optional;

public sealed interface ShowEntityCommand extends Serializable {

    record ShowCommandEnvelope(ShowCommand command, ActorRef<ShowEntityResponse> replyTo) implements ShowEntityCommand {

    }

    record GetShow(ActorRef<Optional<Show>> replyTo) implements ShowEntityCommand {

    }
}
