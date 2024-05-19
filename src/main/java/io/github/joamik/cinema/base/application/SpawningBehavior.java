package io.github.joamik.cinema.base.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.scaladsl.Behaviors;

public class SpawningBehavior {

    private SpawningBehavior() {
    }

    public static Behavior<SpawnProtocol.Command> create() {
        return Behaviors.setup(_ -> SpawnProtocol.create());
    }
}
