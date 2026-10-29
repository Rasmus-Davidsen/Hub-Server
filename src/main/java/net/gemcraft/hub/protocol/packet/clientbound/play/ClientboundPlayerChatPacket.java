package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.protocol.packet.serverbound.play.ServerboundChatPacket;
import net.gemcraft.hub.world.NamespacedKey;
import net.gemcraft.hub.world.entity.player.Player;
import net.gemcraft.hub.world.registry.Registries;
import net.gemcraft.hub.world.registry.Registry;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ClientboundPlayerChatPacket(@NotNull ServerboundChatPacket packet, @NotNull Player sender, @NotNull Component displayName,
                                          int index, int senderIndex, @NotNull List<byte[]> previous) implements ClientboundPacket
{
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        encodeHead(msg);
        encodeBody(msg);
        encodePrevious(msg);

        // Unsigned Content
        msg.writeBoolean(false);

        // Filter Type
        msg.writeEnum(Filter.PASS_THROUGH);

        encodeFormatting(msg);
    }

    private void encodeHead(final @NotNull ByteMessage msg) {
        msg.writeVarInt(index);          // Message index
        msg.writeUUID(sender.getUUID()); // Sender
        msg.writeVarInt(senderIndex);    // Sender index
        encodeSignature(msg);            // Signature
    }

    private void encodeSignature(final @NotNull ByteMessage msg) {
        final byte[] signature = packet.signature();
        msg.writeBoolean(signature != null);

        if(signature != null) {
            msg.writeBytes(signature);
        }
    }

    private void encodeBody(final @NotNull ByteMessage msg) {
        msg.writeString(packet.content()); // Content
        msg.writeLong(packet.timestamp()); // Timestamp
        msg.writeLong(packet.salt());      // Salt
    }

    private void encodePrevious(final @NotNull ByteMessage msg) {
        msg.writeVarInt(previous.size());

        for(final byte[] signature : previous) {
            msg.writeVarInt(0); // Full signature follows
            msg.writeBytes(signature);
        }
    }

    private void encodeFormatting(final @NotNull ByteMessage msg) {
        encodeChatType(msg);

        // Sender name
        msg.writeComponent(displayName);

        // Target name
        msg.writeBoolean(false);
    }

    private void encodeChatType(final @NotNull ByteMessage msg) {
        // Chat type
        final Registry registry = Registries.get(Registry.Type.CHAT_TYPE);

        if(registry == null) {
            throw new IllegalStateException(
                    "Chat type registry is missing"
            );
        }

        final NamespacedKey resource = new NamespacedKey(
                NamespacedKey.GEMCRAFT, "chat"
        );

        final Integer id = registry.getIndex(resource);

        if(id == null) {
            throw new IllegalStateException(
                    "Index not found in chat type registry for " + resource
            );
        }

        msg.writeVarInt(id + 1);
    }

    private enum Filter {
        PASS_THROUGH, FULLY_FILTERED, PARTIALLY_FILTERED
    }
}
