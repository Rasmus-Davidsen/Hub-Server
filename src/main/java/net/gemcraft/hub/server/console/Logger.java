package net.gemcraft.hub.server.console;

import net.gemcraft.hub.Hub;
import net.gemcraft.hub.utils.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// TODO replace with slf4j and logback (Copy from Discord bot)
public class Logger {
    private static final @NotNull String ANSI_RESET = "[0m";
    private static final @NotNull String ANSI_SGR_END = "m";
    private static final @NotNull String ANSI_FG = "[38;2";

    public void info(final @NotNull String msg) {
        log(msg, LogType.INFO);
    }

    public void warn(final @NotNull String msg) {
        log(msg, LogType.WARN);
    }

    private void log(final @NotNull String msg, final @NotNull LogType type) {
        final String formattedMsg = formatMsg(
                msg, type
        );

        final String coloredMsg = colorMsg(
                formattedMsg, type
        );

        Hub.getConsole().println(
                coloredMsg
        );

        Hub.getLogManager().writeLn(
                formattedMsg
        );
    }

    private @NotNull String formatMsg(final @NotNull String msg, final @NotNull LogType type) {
        return String.format(
                "[%s %s]: %s",
                getTime(), type, msg
        );
    }

    private @NotNull String getTime() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "HH:mm:ss"
        );

        return LocalTime.now().format(
                formatter
        );
    }

    private @NotNull String colorMsg(final @NotNull String formattedMsg, final @NotNull LogType type) {
        if(type.getColor() == null) {
            return formattedMsg;
        }

        final String color = type
                .getColor()
                .toAnsi();

        return color + formattedMsg + TextUtils.ANSI_ESC + ANSI_RESET;
    }

    private enum LogType {
        INFO(null),
        WARN(255, 255, 170),
        ERROR(255, 85, 85);

        private final @Nullable Color color;

        LogType(final @Range(from = 0, to = 255) int red, final @Range(from = 0, to = 255) int green, final @Range(from = 0, to = 255) int blue) {
            final Color color = new Color(
                    red, green, blue
            );

            this(color);
        }

        LogType(final @Nullable Color color) {
            this.color = color;
        }

        public @Nullable Color getColor() {
            return color;
        }
    }

    private record Color(@Range(from = 0, to = 255) int red, @Range(from = 0, to = 255) int green, @Range(from = 0, to = 255) int blue) {
        public @NotNull String toAnsi() {
            return String.format(
                    "%s%s;%d;%d;%d%s", TextUtils.ANSI_ESC,
                    ANSI_FG, red, green, blue, ANSI_SGR_END
            );
        }
    }
}