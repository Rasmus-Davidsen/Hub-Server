package net.gemcraft.hub.world;

import net.gemcraft.hub.world.registry.BlockRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public class LightEngine {
    private static final int MAX_HEIGHT = 256;
    private static final int PADDING = 16;

    private final int minX, minY, minZ;
    private final int lenX, lenZ;

    private final byte[] blockLight;

    public LightEngine(final @NotNull Schematic schematic) {
        final BlockVec3 placement = schematic.getPlacement();
        final BlockVec3 offset = schematic.getOffset();

        minX = placement.x() + offset.x() - PADDING;
        minY = placement.y() + offset.y();
        minZ = placement.z() + offset.z() - PADDING;

        lenX = schematic.getWidth() + PADDING * 2;
        lenZ = schematic.getLength() + PADDING * 2;

        blockLight = calculateBlockLight(schematic);
    }

    private byte[] calculateBlockLight(final @NotNull Schematic schematic) {
        final int blockCount = lenX * lenZ * MAX_HEIGHT;
        final byte[] blockLight = new byte[blockCount];

        final boolean[] opaque = new boolean[blockCount];
        final ArrayDeque<Integer> blockQueue = new ArrayDeque<>();

        final BlockRegistry blockRegistry = schematic.getBlockRegistry();

        int idx = 0;

        for(int relY = 0; relY < MAX_HEIGHT; relY++) {
            final int y = relY - minY;

            for(int relZ = 0; relZ < lenZ; relZ++) {
                final int z = relZ - PADDING;

                for(int relX = 0; relX < lenX; relX++) {
                    final int x = relX - PADDING;
                    final int id = schematic.getBlock(x, y, z);

                    // Opaque
                    opaque[idx] = blockRegistry.isOpaque(id);

                    // Emission
                    final int emission = blockRegistry.getEmission(id);

                    if(emission > 0) {
                        blockLight[idx] = (byte) emission;
                        blockQueue.add(idx);
                    }

                    idx++;
                }
            }
        }

        flood(blockLight, blockQueue, opaque);
        return blockLight;
    }

    private void flood(final byte[] lightData, final @NotNull ArrayDeque<@NotNull Integer> queue, final boolean[] opaque) {
        while(!queue.isEmpty()) {
            final int i = queue.poll();

            final int x = unpackX(i);
            final int y = unpackY(i);
            final int z = unpackZ(i);

            final int level = lightData[i];

            if(level <= 1) {
                continue;
            }

            // Flood in all directions
            for(final Direction direction : Direction.values()) {
                // Find adjacent block in current direction
                final BlockVec3 step = direction.getNormal();

                final int adjacentX = x + step.x();
                final int adjacentY = y + step.y();
                final int adjacentZ = z + step.z();

                if(adjacentX < 0 || adjacentX >= lenX
                        || adjacentY < 0 || adjacentY >= MAX_HEIGHT
                        || adjacentZ < 0 || adjacentZ >= lenZ)
                {
                    continue;
                }

                // Stop at opaque blocks
                final int idx = pack(
                        adjacentX, adjacentY, adjacentZ
                );

                if(opaque[idx]) {
                    continue;
                }

                // Apply highest light level
                final int nextLevel = level - 1;

                if(nextLevel > lightData[idx]) {
                    lightData[idx] = (byte) nextLevel;
                    queue.add(idx);
                }
            }
        }
    }

    private int pack(final int x, final int y, final int z) {
        return (y * lenZ + z) * lenX + x;
    }

    private int unpackX(final int index) {
        return index % lenX;
    }

    private int unpackY(final int index) {
        return index / (lenX * lenZ);
    }

    private int unpackZ(final int index) {
        return (index / lenX) % lenZ;
    }

    public int getBlockLight(final int x, final int y, final int z) {
        if(y < 0 || y >= MAX_HEIGHT) {
            return 0;
        }

        final int relX = x - minX;
        final int relZ = z - minZ;

        if(relX < 0 || relX >= lenX || relZ < 0 || relZ >= lenZ) {
            return 0;
        }

        final int idx = pack(
                relX, y, relZ
        );

        return blockLight[idx];
    }
}