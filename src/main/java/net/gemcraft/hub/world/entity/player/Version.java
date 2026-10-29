package net.gemcraft.hub.world.entity.player;

import org.jetbrains.annotations.NotNull;

/**
 * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol_version_numbers">Protocol Versions</a>
 */
public enum Version {
    V1_21_to_V1_21_1(767, "1.21 - 1.21.1"),       // 1.21 - 1.21.1
    V1_21_2_to_V1_21_3(768, "1.21.2 - 1.21.3"),   // 1.21.2 - 1.21.3
    V1_21_4(769, "1.21.4"),                       // 1.21.4
    V1_21_5(770, "1.21.5"),                       // 1.21.5
    V1_21_6(771, "1.21.6"),                       // 1.21.6
    V1_21_7_to_V1_21_8(772, "1.21.7 - 1.21.8"),   // 1.21.7 - 1.21.8
    V1_21_9_to_V1_21_10(773, "1.21.9 - 1.21.10"), // 1.21.9 - 1.21.10
    V1_21_11(774, "1.21.11"),                     // 1.21.11
    V26_1_to_26_1_2(775, "26.1 - 26.1.2"),        // 26.1 - 26.1.2
    V26_2(776, "26.2"),                           // 26.2
    V26_3(777, "26.3"),                           // 26.3
    UNKNOWN(-1, "Unknown");                       // Unknown

    private final int protocol;
    private final @NotNull String name;

    Version(final int protocol, final @NotNull String name) {
        this.protocol = protocol;
        this.name = name;
    }

    public int getProtocol() {
        return protocol;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getHighestName() {
        final String[] parts = getName().split(" ");

        final int i = parts.length - 1;

        return parts[i];
    }

    public static @NotNull Version getLatest() {
        final Version[] versions = Version.values();

        final int i = versions.length - 2; // Also skip "Unknown"

        return versions[i];
    }
}
