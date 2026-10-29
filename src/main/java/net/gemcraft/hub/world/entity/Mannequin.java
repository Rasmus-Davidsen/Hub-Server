package net.gemcraft.hub.world.entity;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.world.Location;
import net.gemcraft.hub.world.entity.player.Skin;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Mannequin extends Avatar {
    private @Nullable ResolvableProfile profile;
    private boolean immovable = false;
    private @Nullable Component description;

    public Mannequin(final @NotNull Location location) {
        super(EntityType.MANNEQUIN, location);
    }

    public void setProfile(final @Nullable ResolvableProfile profile) {
        this.profile = profile;
    }

    public @Nullable ResolvableProfile getProfile() {
        return profile;
    }

    public void setImmovable(final boolean immovable) {
        this.immovable = immovable;
    }

    public boolean isImmovable() {
        return immovable;
    }

    public void setDescription(final @Nullable Component description) {
        this.description = description;
    }

    public @Nullable Component getDescription() {
        return description;
    }

    @Override
    public void encodeData(final @NotNull ByteMessage msg) {
        super.encodeData(msg);

        // Immovable
        msg.writeByte(MannequinComponent.IMMOVABLE.getIndex());
        msg.writeVarInt(MannequinComponent.IMMOVABLE.getType().getID());
        msg.writeBoolean(immovable);

        // Description
        if(description != null) {
            msg.writeByte(MannequinComponent.DESCRIPTION.getIndex());
            msg.writeVarInt(MannequinComponent.DESCRIPTION.getType().getID());
            msg.writeBoolean(true);
            msg.writeComponent(description);
        }

        // Resolvable profile
        if(profile != null) {
            msg.writeByte(MannequinComponent.RESOLVABLE_PROFILE.getIndex());
            msg.writeVarInt(MannequinComponent.RESOLVABLE_PROFILE.getType().getID());
            msg.writeProfile(profile);
        }
    }

    public enum MannequinComponent implements DataComponent {
        RESOLVABLE_PROFILE(17, DataType.RESOLVABLE_PROFILE),
        IMMOVABLE(18, DataType.BOOLEAN),
        DESCRIPTION(19, DataType.OPTIONAL_TEXT_COMPONENT);

        private final int index;
        private final @NotNull DataType type;

        MannequinComponent(final int index, final @NotNull DataType type) {
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

    public record ResolvableProfile(@NotNull Skin skin) {}
}
