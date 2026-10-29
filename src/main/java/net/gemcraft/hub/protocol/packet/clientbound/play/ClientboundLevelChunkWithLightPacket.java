package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.BlockVec3;
import net.gemcraft.hub.world.LightEngine;
import net.gemcraft.hub.world.Schematic;
import net.gemcraft.hub.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record ClientboundLevelChunkWithLightPacket(int chunkX, int chunkZ, @NotNull World world, int biomeId) implements ClientboundPacket {
    private static final int WORLD_HEIGHT = 256;

    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeInt(chunkX);  // Chunk X
        msg.writeInt(chunkZ);  // Chunk Z
        encodeHeightmaps(msg); // Heightmaps
        encodeSections(msg);   // Sections
        msg.writeVarInt(0);    // Block entities
        encodeLight(msg);      // Light
    }

    private void encodeHeightmaps(final @NotNull ByteMessage msg) {
        msg.writeVarInt(1); // Length prefix
        msg.writeEnum(HeightMap.MOTION_BLOCKING); // Heightmap

        // Heightmap data
        final long[] data = new long[WORLD_HEIGHT];
        msg.writeLongArray(data);
    }

    private void encodeSections(final @NotNull ByteMessage msg) {
        final ByteMessage sections = new ByteMessage(
                msg.alloc().buffer()
        );

        final Schematic schematic = world.getSchematic();

        final BlockVec3 origin = schematic.getPlacement();
        final BlockVec3 offset = schematic.getOffset();

        final int originX = origin.x() + offset.x();
        final int originY = origin.y() + offset.y();
        final int originZ = origin.z() + offset.z();

        final int baseX = (chunkX << 4) - originX;
        final int baseZ = (chunkZ << 4) - originZ;

        try {
            for(int subChunkY = 0; subChunkY < WORLD_HEIGHT / 16; subChunkY++) {
                final int baseY = (subChunkY << 4) - originY;

                final int[] states = new int[4096];
                int blockCount = 0;

                int idx = 0;

                for(int y = 0; y < 16; y++) {
                    final int worldY = baseY + y;

                    for(int z = 0; z < 16; z++) {
                        final int worldZ = baseZ + z;

                        for(int x = 0; x < 16; x++) {
                            final int worldX = baseX + x;

                            final int id = schematic.getBlock(
                                    worldX, worldY, worldZ
                            );

                            states[idx++] = id;

                            if(id != 0) { // Not air
                                blockCount++;
                            }
                        }
                    }
                }

                sections.writeShort(blockCount);    // Block count
                sections.writeShort(0);             // Fluid count
                writeBlockStates(sections, states); // Block states

                // Biomes
                sections.writeByte(0);         // Single-valued
                sections.writeVarInt(biomeId); // Biome
            }

            msg.writeVarInt(sections.readableBytes());
            msg.writeBytes(sections);
        } finally {
            sections.release();
        }
    }

    private void writeBlockStates(final @NotNull ByteMessage out, final int @NotNull[] states) {
        final Map<Integer, Integer> palette = new LinkedHashMap<>();

        for(final int state : states) {
            palette.putIfAbsent(
                    state, palette.size()
            );
        }

        // Single-valued
        if(palette.size() == 1) {
            out.writeByte(0);
            out.writeVarInt(states[0]);
            return;
        }

        final boolean isDirect = palette.size() > 256;
        byte bits = 15;

        if(!isDirect) {
            final int leadingZeros = Integer.numberOfLeadingZeros(
                    palette.size() - 1
            );

            bits = (byte) Math.max(
                    4, 32 - leadingZeros
            );
        }

        out.writeByte(bits);

        if(!isDirect) {
            out.writeVarInt(palette.size());

            palette.keySet().forEach(
                    out::writeVarInt
            );
        }

        // Entries never span two longs
        final int perLong = 64 / bits;
        final int len = (4096 + perLong - 1) / perLong;

        final long[] packed = new long[len];

        for(int i = 0; i < 4096; i++) {
            final long value = isDirect
                    ? states[i]
                    : palette.get(states[i]);

            packed[i / perLong] |= value << ((i % perLong) * bits);
        }

        for(final long l : packed) {
            out.writeLong(l);
        }
    }

    private void encodeLight(final @NotNull ByteMessage msg) {
        final LightEngine lightEngine = world.getLightEngine();

        final BitSet blockMask = new BitSet();
        final BitSet emptyMask = new BitSet();
        final List<byte[]> blockLight = new ArrayList<>();

        final int baseX = chunkX << 4;
        final int baseZ = chunkZ << 4;

        for(int subchunkY = -1; subchunkY < WORLD_HEIGHT / 16 + 1; subchunkY++) {
            final int baseY = subchunkY << 4;

            final byte[] blocks = new byte[2048];
            boolean hasBlocks = false;

            int idx = 0;

            for(int y = 0; y < 16; y++) {
                final int worldY = baseY + y;

                for(int z = 0; z < 16; z++) {
                    final int worldZ = baseZ + z;

                    for(int x = 0; x < 16; x++) {
                        final int worldX = baseX + x;

                        final int block = lightEngine.getBlockLight(
                                worldX, worldY, worldZ
                        );

                        blocks[idx >> 1] |= (byte) (block << ((idx & 1) * 4));
                        idx++;

                        hasBlocks |= block != 0;
                    }
                }
            }

            if(hasBlocks) {
                blockMask.set(subchunkY + 1);
                blockLight.add(blocks);
            } else {
                emptyMask.set(subchunkY + 1);
            }
        }

        msg.writeVarInt(0); // Sky mask
        writeBitSet(msg, blockMask); // Block mask

        msg.writeVarInt(0); // Empty sky mask
        writeBitSet(msg, emptyMask); // Empty block mask

        // Skylight
        msg.writeVarInt(0);

        // Block light
        msg.writeVarInt(blockLight.size());

        for(final byte[] light : blockLight) {
            msg.writeVarInt(2048);
            msg.writeBytes(light);
        }
    }

    private static void writeBitSet(final @NotNull ByteMessage msg, final @NotNull BitSet bitSet) {
        final byte[] bytes = bitSet.toByteArray();
        msg.writeVarInt(bytes.length);
        msg.writeBytes(bytes);
    }

    enum HeightMap {
        WORLD_SURFACE_WG, WORLD_SURFACE, OCEAN_FLOOR_WG,
        OCEAN_FLOOR, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES
    }
}
