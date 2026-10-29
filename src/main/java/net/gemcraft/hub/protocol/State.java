package net.gemcraft.hub.protocol;

import net.gemcraft.hub.protocol.packet.PacketFlow;
import net.gemcraft.hub.protocol.packet.PacketRegistry;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.protocol.packet.clientbound.common.ClientboundCustomPayloadPacket;
import net.gemcraft.hub.protocol.packet.clientbound.common.ClientboundDisconnectPacket;
import net.gemcraft.hub.protocol.packet.clientbound.common.ClientboundKeepAlivePacket;
import net.gemcraft.hub.protocol.packet.clientbound.configuration.ClientboundFinishConfiguration;
import net.gemcraft.hub.protocol.packet.clientbound.configuration.ClientboundRegistryDataPacket;
import net.gemcraft.hub.protocol.packet.clientbound.configuration.ClientboundUpdateTagsPacket;
import net.gemcraft.hub.protocol.packet.clientbound.login.ClientboundCustomQueryPacket;
import net.gemcraft.hub.protocol.packet.clientbound.login.ClientboundLoginDisconnectPacket;
import net.gemcraft.hub.protocol.packet.clientbound.login.ClientboundLoginFinishedPacket;
import net.gemcraft.hub.protocol.packet.clientbound.play.*;
import net.gemcraft.hub.protocol.packet.clientbound.status.ClientboundPingPacket;
import net.gemcraft.hub.protocol.packet.clientbound.status.ClientboundPongResponsePacket;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import net.gemcraft.hub.protocol.packet.serverbound.common.ServerboundClientInformationPacket;
import net.gemcraft.hub.protocol.packet.serverbound.common.ServerboundKeepAlivePacket;
import net.gemcraft.hub.protocol.packet.serverbound.configuration.ServerboundFinishConfigurationPacket;
import net.gemcraft.hub.protocol.packet.serverbound.handshake.ServerboundHandshakePacket;
import net.gemcraft.hub.protocol.packet.serverbound.login.ServerboundCustomQueryAnswerPacket;
import net.gemcraft.hub.protocol.packet.serverbound.login.ServerboundHelloPacket;
import net.gemcraft.hub.protocol.packet.serverbound.login.ServerboundLoginAcknowledgedPacket;
import net.gemcraft.hub.protocol.packet.serverbound.play.ServerboundChatPacket;
import net.gemcraft.hub.protocol.packet.serverbound.play.ServerboundChatSessionUpdatePacket;
import net.gemcraft.hub.protocol.packet.serverbound.play.ServerboundInteractPacket;
import net.gemcraft.hub.protocol.packet.serverbound.status.ServerboundPingPacket;
import net.gemcraft.hub.protocol.packet.serverbound.status.ServerboundPingRequestPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets">Packet Protocol</a>
 */
public enum State {
    HANDSHAKE(
            Map.ofEntries(), // Clientbound
            Map.ofEntries( // Serverbound
                    Map.entry("intention", ServerboundHandshakePacket.class)
            )
    ),
    STATUS(
            Map.ofEntries( // Clientbound
                    Map.entry("status_response", ClientboundPongResponsePacket.class),
                    Map.entry("pong_response", ClientboundPingPacket.class)
            ),
            Map.ofEntries( // Serverbound
                    Map.entry("status_request", ServerboundPingRequestPacket.class),
                    Map.entry("ping_request", ServerboundPingPacket.class)
            )
    ),
    LOGIN(
            Map.ofEntries( // Clientbound
                    Map.entry("login_disconnect", ClientboundLoginDisconnectPacket.class),
                    Map.entry("login_finished", ClientboundLoginFinishedPacket.class),
                    Map.entry("custom_query", ClientboundCustomQueryPacket.class)
            ),
            Map.ofEntries( // Serverbound
                    Map.entry("hello", ServerboundHelloPacket.class),
                    Map.entry("custom_query_answer", ServerboundCustomQueryAnswerPacket.class),
                    Map.entry("login_acknowledged", ServerboundLoginAcknowledgedPacket.class)
            )
    ),
    CONFIGURATION(
            Map.ofEntries( // Clientbound
                    Map.entry("custom_payload", ClientboundCustomPayloadPacket.class),
                    Map.entry("disconnect", ClientboundDisconnectPacket.class),
                    Map.entry("finish_configuration", ClientboundFinishConfiguration.class),
                    Map.entry("keep_alive", ClientboundKeepAlivePacket.class),
                    Map.entry("registry_data", ClientboundRegistryDataPacket.class),
                    Map.entry("update_tags", ClientboundUpdateTagsPacket.class)
            ),
            Map.ofEntries( // Serverbound
                    Map.entry("client_information", ServerboundClientInformationPacket.class),
                    Map.entry("finish_configuration", ServerboundFinishConfigurationPacket.class),
                    Map.entry("keep_alive", ServerboundKeepAlivePacket.class)
            )
    ),
    PLAY(
            Map.ofEntries( // Clientbound
                    Map.entry("add_entity", ClientboundAddEntityPacket.class),
                    Map.entry("animate", ClientboundAnimatePacket.class),
                    Map.entry("commands", ClientboundCommandsPacket.class),
                    Map.entry("custom_payload", ClientboundCustomPayloadPacket.class),
                    Map.entry("disconnect", ClientboundDisconnectPacket.class),
                    Map.entry("game_event", ClientboundGameEventPacket.class),
                    Map.entry("keep_alive", ClientboundKeepAlivePacket.class),
                    Map.entry("level_chunk_with_light", ClientboundLevelChunkWithLightPacket.class),
                    Map.entry("login", ClientboundLoginPacket.class),
                    Map.entry("player_abilities", ClientboundPlayerAbilitiesPacket.class),
                    Map.entry("player_chat", ClientboundPlayerChatPacket.class),
                    Map.entry("player_info_update", ClientboundPlayerInfoUpdatePacket.class),
                    Map.entry("player_position", ClientboundPlayerPositionPacket.class),
                    Map.entry("set_entity_data", ClientboundSetEntityDataPacket.class),
                    Map.entry("set_player_team", ClientboundSetPlayerTeamPacket.class),
                    Map.entry("set_time", ClientboundSetTimePacket.class),
                    Map.entry("system_chat", ClientboundSystemChatPacket.class)
            ),
            Map.ofEntries( // Serverbound
                    Map.entry("chat", ServerboundChatPacket.class),
                    Map.entry("chat_session_update", ServerboundChatSessionUpdatePacket.class),
                    Map.entry("client_information", ServerboundClientInformationPacket.class),
                    Map.entry("interact", ServerboundInteractPacket.class),
                    Map.entry("keep_alive", ServerboundKeepAlivePacket.class)
            )
    );

    private final @NotNull PacketRegistry<ClientboundPacket> clientboundRegistry;
    private final @NotNull PacketRegistry<ServerboundPacket> serverboundRegistry;

    State(final @NotNull Map<@NotNull String, @NotNull Class<? extends ClientboundPacket>> clientbound,
          final @NotNull Map<@NotNull String, @NotNull Class<? extends ServerboundPacket>> serverbound)
    {
        clientboundRegistry = PacketRegistry.of(
                this, PacketFlow.CLIENTBOUND, clientbound
        );

        serverboundRegistry = PacketRegistry.of(
                this, PacketFlow.SERVERBOUND, serverbound
        );
    }

    public @NotNull PacketRegistry<ClientboundPacket> getClientboundRegistry() {
        return clientboundRegistry;
    }

    public @NotNull PacketRegistry<ServerboundPacket> getServerboundRegistry() {
        return serverboundRegistry;
    }

    public static @NotNull State getState(final int id) {
        if(id < values().length) {
            return values()[id];
        }

        return State.PLAY;
    }
}
