package it.unicam.cs.mpgc.rpg132007.core;

import java.util.List;

/** Aspetto di un combattente: sprite ASCII e colore (es. "#ff4444"). Come disegnarlo lo decide la view. */
public record Appearance(List<String> sprite, String color) {

    public Appearance {
        sprite = List.copyOf(sprite);
    }
}
