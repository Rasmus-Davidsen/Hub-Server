package net.gemcraft.hub.utils;

import net.gemcraft.hub.world.entity.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class TextUtils {
    public static final @NotNull String UNKNOWN = "N/A";
    public static final @NotNull TextColor SERVER_COLOR = TextColor.color(0x3F41FF);
    public static final char ANSI_ESC = '\033';

    /**
     * @param text Text including any hex colors, color codes and decoration codes
     * @return Component formatted with included hex colors, color codes, and decoration codes
     */
    public static @NotNull Component colors(final @NotNull String text) {
        final Component coloredText = MiniMessage
                .miniMessage()
                .deserialize(text);

        return withoutDefaultItalic(
                coloredText
        );
    }

    /**
     * @param text Text to color
     * @param color Color of the message
     * @param decorations Decorations for the message
     * @return Message as a component with the given color and decorations
     */
    public static @NotNull Component simpleColor(final @NotNull String text, final @NotNull TextColor color, final @NotNull TextDecoration @NotNull... decorations) {
        return withoutDefaultItalic(Component.text(
                text, color, decorations
        ));
    }

    public static @NotNull Component gradient(final @NotNull String text, final @NotNull String startColor, final @NotNull String endColor,
                                               final @NotNull TextDecoration @NotNull... decorations) {
        final String gradient = String.format(
                "<gradient:%s:%s>", startColor, endColor
        );

        return TextUtils
                .colors(gradient + text)
                .decorate(decorations);
    }

    /**
     * @param text Text to wrap
     * @return Wrapped component to neutralize any default formatting
     */
    public static @NotNull Component withoutDefaultItalic(final @NotNull Component text) {
        final List<Component> children = text.children()
                .stream()
                .map(TextUtils::withoutDefaultItalic)
                .collect(Collectors.toList());

        return text.children(children).decorationIfAbsent(
                TextDecoration.ITALIC, TextDecoration.State.FALSE
        );
    }

    public static @NotNull Component asServerText(final @NotNull String text) {
        return TextUtils.simpleColor(
                text, SERVER_COLOR, TextDecoration.BOLD
        );
    }

    public static @NotNull Component profile(final @NotNull Player player) {
        return Component.text(
                player.getName(), NamedTextColor.AQUA
        );
    }

    private static final @NotNull JoinConfiguration JOIN_CONFIG = JoinConfiguration.spaces();

    public static @NotNull Component join(final @NotNull Component @NotNull ... parts) {
        return Component.join(
                JOIN_CONFIG, parts
        );
    }
}
