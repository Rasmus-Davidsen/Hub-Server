package net.gemcraft.hub.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Date;

public class DateUtils {
    /**
     * @return Current date instance
     */
    public static @NotNull Instant getInstant() {
        return Instant.now();
    }

    /**
     * @return Current date
     */
    public static @NotNull Date getDate() {
        return Date.from(
                getInstant()
        );
    }

    /**
     * @param from Date to compare from
     * @return Milliseconds from the compare date to now
     * @apiNote Negative result if it's before the current date
     */
    public static long msSince(final @NotNull Date from) {
        return DateUtils.msBetween(
                from, getDate()
        );
    }

    /**
     * @param from Date to compare from
     * @param to Date to compare to
     * @return Milliseconds from the first date to the other
     * @apiNote Negative result if "from" is after "to"
     */
    public static long msBetween(final @NotNull Date from, final @NotNull Date to) {
        return to.getTime() - from.getTime();
    }
}
