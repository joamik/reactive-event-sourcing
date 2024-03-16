package io.github.joamik.cinema.domain;

import java.io.Serializable;

public sealed interface ShowCommand extends Serializable {

    record ReserveSeat(ShowId showId, SeatNumber seatNumber) implements ShowCommand {

    }
}
