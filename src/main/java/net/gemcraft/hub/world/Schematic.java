package net.gemcraft.hub.world;

import net.gemcraft.hub.Hub;
import net.gemcraft.hub.world.registry.BlockRegistry;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Schematic {
    private final @NotNull BlockRegistry blockRegistry;
    private final @NotNull BlockVec3 placement;
    private final @NotNull BlockVec3 offset;
    private final int width, height, length;
    private final int[] blocks;

    private Schematic(final @NotNull BlockRegistry blockRegistry, final @NotNull BlockVec3 placement) {
        this(blockRegistry, placement, 0, 0, 0, BlockVec3.EMPTY, new int[0]);
    }

    private Schematic(final @NotNull BlockRegistry blockRegistry, final @NotNull BlockVec3 placement, final int width, final int height,
                      final int length, final @NotNull BlockVec3 offset, final int @NotNull[] blocks)
    {
        this.blockRegistry = blockRegistry;

        this.placement = placement;
        this.offset = offset;

        this.width = width;
        this.height = height;
        this.length = length;

        this.blocks = blocks;
    }

    public @NotNull BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    public @NotNull BlockVec3 getPlacement() {
        return placement;
    }

    public int getWidth()  {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public @NotNull BlockVec3 getOffset() {
        return offset;
    }

    public int getBlock(final int x, final int y, final int z) {
        if(x < 0 || x >= width) {
            return 0;
        }

        if(y < 0 || y >= height) {
            return 0;
        }

        if(z < 0 || z >= length) {
            return 0;
        }

        return blocks[(y * length + z) * width + x];
    }

    public static @NotNull Schematic load(final @NotNull Path path, final @NotNull BlockVec3 placement, final @NotNull BlockRegistry blockRegistry) throws IOException {
        final File file = path.toFile();

        if(!file.exists()) {
            Hub.getLogger().info("Schematic not found, using empty world");

            return new Schematic(
                    blockRegistry, placement
            );
        }

        CompoundTag root = (CompoundTag) NBTUtil.read(file).getTag();

        if(root.containsKey("Schematic")) {
            root = root.getCompoundTag("Schematic");
        }

        final int width = root.getShort("Width");
        final int height = root.getShort("Height");
        final int len = root.getShort("Length");

        final BlockVec3 offset = getOffset(root);

        final CompoundTag blocksTag = root.getCompoundTag("Blocks");
        final CompoundTag palette = blocksTag.getCompoundTag("Palette");
        final byte[] data = blocksTag.getByteArray("Data");

        // Schematic palette index -> internal block state ID
        final Map<Integer, Integer> remap = new HashMap<>();

        palette.forEach((name, tag) -> remap.put(
                ((IntTag) tag).asInt(), blockRegistry.get(name)
        ));

        // BlockData is a stream of VarInts
        final int blockCount = width * height * len;
        final int[] blocks = new int[blockCount];

        int idx = 0;

        for(int i = 0; i < blocks.length; i++) {
            int value = 0, shift = 0, block;

            do {
                block = data[idx++];
                value |= (block & 0x7F) << shift;
                shift += 7;
            } while((block & 0x80) != 0);

            blocks[i] = remap.getOrDefault(
                    value, 0
            );
        }

        return new Schematic(
                blockRegistry, placement, width,
                height, len, offset, blocks
        );
    }

    private static @NotNull BlockVec3 getOffset(final @NotNull CompoundTag root) {
        final int[] offset = root.getIntArray("Offset");

        if(offset.length != 3) {
            return BlockVec3.EMPTY;
        }

        return new BlockVec3(
                offset[0], offset[1], offset[2]
        );
    }
}
