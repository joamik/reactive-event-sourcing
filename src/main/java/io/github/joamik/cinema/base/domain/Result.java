package io.github.joamik.cinema.base.domain;

public sealed interface Result<E, V> {

    static <E, V> Success<E, V> success(V value) {
        return new Success<>(value);
    }

    static <E, V> Failure<E, V> failure(E error) {
        return new Failure<>(error);
    }

    record Success<E, V>(V value) implements Result<E, V> {

    }

    record Failure<E, V>(E error) implements Result<E, V> {

    }
}
