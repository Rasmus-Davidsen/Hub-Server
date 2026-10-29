package net.gemcraft.hub.world.entity.player;

import net.gemcraft.hub.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

public enum Rank {
    SAPPHIRE(0, "Sapphire", "#1144FF", "#4477FF"),
    JADE(1, "Jade", "#11FF11", "#77FF77"),
    CITRINE(2, "Citrine", "#FFFF33", "#FFFF88"),
    PAINITE(3, "Painite", "#FF5555", "#FF7777"),
    WITHERITE(4, "Witherite", "#444444", "white"),
    BISMUTH(5, "Bismuth", TextUtils.colors("<#FF8888>B<#FFBB55>i<#FFFF88>s<#99FF88>m<#88FFFF>u<#88BBFF>t<#FFBBFF>h")),

    YOUTUBE(20, "YouTube", TextUtils.colors("<white>You<#FF0033>Tube")),

    MOD(50, "Mod", "#1199FF", "#55CCFF"),
    ADMIN(51, "Admin", "#FF4433", "#FF6655");

    private final int id;
    private final @NotNull String name;
    private final @NotNull Component prefix;

    Rank(final int id, final @NotNull String name, final @NotNull String startColor, final @NotNull String endColor) {
        this.id = id;
        this.name = name;
        this.prefix = TextUtils.gradient(
                name, startColor, endColor, TextDecoration.BOLD
        );
    }

    Rank(final int id, final @NotNull String name, final @NotNull Component prefix) {
        this.id = id;
        this.name = name;
        this.prefix = prefix.decorate(
                TextDecoration.BOLD
        );
    }

    public int getId() {
        return id;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Component getPrefix() {
        return prefix;
    }
}