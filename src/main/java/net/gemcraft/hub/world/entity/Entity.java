package net.gemcraft.hub.world.entity;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.world.Location;
import net.gemcraft.hub.world.Vec3;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Entity implements DataComponentHolder {
    private static final int EMPTY_DATA = 0;
    private static final @NotNull AtomicInteger ENTITY_ID = new AtomicInteger(1000); // Avoid player id

    private final @NotNull EntityType type;
    private final @NotNull Location location;
    private final @NotNull UUID uuid;
    private final int entityID;
    private final @NotNull Vec3 velocity = Vec3.EMPTY;
    private final Set<@NotNull String> tags = new HashSet<>();

    private @Nullable Component customName;
    private boolean customNameVisible = false;

    public Entity(final @NotNull EntityType type, final @NotNull Location location) {
        this.type = type;
        this.location = location;
        uuid = UUID.randomUUID();
        entityID = ENTITY_ID.getAndIncrement();
    }

    public @NotNull EntityType getType() {
        return type;
    }

    public @NotNull UUID getUUID() {
        return uuid;
    }

    public int getEntityID() {
        return entityID;
    }

    public int getData() {
        return EMPTY_DATA;
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public @NotNull Vec3 getVelocity() {
        return velocity;
    }

    public Set<@NotNull String> getTags() {
        return tags;
    }

    public void setCustomName(final @Nullable Component customName) {
        this.customName = customName;
    }

    public @Nullable Component getCustomName() {
        return customName;
    }

    public void setCustomNameVisible(final boolean customNameVisible) {
        this.customNameVisible = customNameVisible;
    }

    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    @Override
    public void encodeData(final @NotNull ByteMessage msg) {
        // Custom name visible
        msg.writeByte(EntityComponent.CUSTOM_NAME_VISIBLE.getIndex());
        msg.writeVarInt(EntityComponent.CUSTOM_NAME_VISIBLE.getType().getID());
        msg.writeBoolean(customNameVisible);

        // Custom name
        if(customName != null) {
            msg.writeByte(EntityComponent.CUSTOM_NAME.getIndex());
            msg.writeVarInt(EntityComponent.CUSTOM_NAME.getType().getID());
            msg.writeBoolean(true);
            msg.writeComponent(customName);
        }
    }

    public enum EntityComponent implements DataComponent {
        CUSTOM_NAME(2, DataType.OPTIONAL_TEXT_COMPONENT),
        CUSTOM_NAME_VISIBLE(3, DataType.BOOLEAN);

        private final int index;
        private final @NotNull DataType type;

        EntityComponent(final int index, final @NotNull DataType type) {
            this.index = index;
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public @NotNull DataType getType() {
            return type;
        }
    }
}
