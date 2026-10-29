package net.gemcraft.hub.protocol.client;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class SignatureTracker {
    public static final int TRACKED = 20;

    private final List<byte[]> tracked = new ArrayList<>(Collections.nCopies(TRACKED, null));

    public void add(final byte[] signature) {
        tracked.add(signature);
    }

    public @NotNull List<byte[]> acknowledge(final int offset, final @NotNull BitSet acknowledged) {
        tracked.subList(0, Math.min(offset, tracked.size())).clear();

        final List<byte[]> signatures = new ArrayList<>();

        for(int i = 0; i < TRACKED && i < tracked.size(); i++) {
            if(acknowledged.get(i)) {
                signatures.add(tracked.get(i));
            }
        }

        return signatures;
    }
}
