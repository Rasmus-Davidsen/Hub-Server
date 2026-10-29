package net.gemcraft.hub.world.entity;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.world.Location;
import net.gemcraft.hub.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public abstract class Avatar extends Entity {
    private @NotNull Set<@NotNull SkinPart> skinParts = Collections.emptySet();
    private Player.@NotNull Hand mainHand = Player.Hand.RIGHT;

    public Avatar(final @NotNull EntityType type, final @NotNull Location location) {
        super(type, location);
    }

    public void setMainHand(final @NotNull Player.Hand hand) {
        mainHand = hand;
    }

    public @NotNull Player.Hand getMainHand() {
        return mainHand;
    }

    public void setSkinParts(final @NotNull Set<@NotNull SkinPart> parts) {
        skinParts = Collections.unmodifiableSet(parts);
    }

    @Unmodifiable
    public @NotNull Set<@NotNull SkinPart> getSkinParts() {
        return skinParts;
    }

    @Override
    public void encodeData(final @NotNull ByteMessage msg) {
        super.encodeData(msg);

        // Main hand
        msg.writeByte(AvatarComponent.MAIN_HAND.getIndex());
        msg.writeVarInt(AvatarComponent.MAIN_HAND.getType().getID());
        msg.writeEnum(mainHand);

        // Skin mask
        msg.writeByte(AvatarComponent.SKIN_MASK.getIndex());
        msg.writeVarInt(AvatarComponent.SKIN_MASK.getType().getID());
        msg.writeByte(SkinPart.getMask(skinParts));
    }

    public enum SkinPart {
        CAPE, JACKET, LEFT_SLEEVE, RIGHT_SLEEVE,
        LEFT_PANTS_LEG, RIGHT_PANTS_LEG, HAT;

        public int getMask() {
            return 1 << ordinal();
        }

        public static @NotNull Set<@NotNull SkinPart> fromMask(final int mask) {
            final Set<SkinPart> parts = EnumSet.noneOf(SkinPart.class);

            for(final SkinPart part : SkinPart.values()) {
                if((mask & part.getMask()) != 0) {
                    parts.add(part);
                }
            }

            return parts;
        }

        public static int getMask(final @NotNull Set<@NotNull SkinPart> parts) {
            int mask = 0;

            for(final SkinPart part : parts) {
                mask |= part.getMask();
            }

            return mask;
        }
    }

    public enum AvatarComponent implements DataComponent {
        MAIN_HAND(15, DataType.HUMANOID_ARM),
        SKIN_MASK(16, DataType.BYTE);

        private final int index;
        private final @NotNull DataType type;

        AvatarComponent(final int index, final @NotNull DataType type) {
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
