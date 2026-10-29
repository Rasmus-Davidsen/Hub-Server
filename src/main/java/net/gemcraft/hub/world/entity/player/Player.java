package net.gemcraft.hub.world.entity.player;

import net.gemcraft.hub.protocol.client.ChatSession;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.client.SignatureTracker;
import net.gemcraft.hub.protocol.packet.clientbound.play.ClientboundSystemChatPacket;
import net.gemcraft.hub.world.Location;
import net.gemcraft.hub.world.World;
import net.gemcraft.hub.world.entity.Avatar;
import net.gemcraft.hub.world.entity.EntityType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Player extends Avatar {
    private final @NotNull ClientConnection connection;
    private final @NotNull GameProfile gameProfile;
    private final @NotNull Version version;
    private @Nullable String clientBrand = null;
    private @NotNull Rank rank = Rank.SAPPHIRE;
    private @NotNull GameMode gameMode = GameMode.ADVENTURE;
    private @Nullable GameMode previousGameMode = null;
    private boolean isListed = true;
    private float flyingSpeed = 0.05f;
    private float fovModifier = 0.1f;
    private int viewDistance = 10;
    private int simulationDistance = 10;
    private int portalCooldown = 0;
    private @Nullable Location deathLocation = null;

    // Chat
    private @Nullable ChatSession chatSession;
    private final SignatureTracker signatureTracker = new SignatureTracker();
    private int receivedMessageIdx = 0;
    private int sendMessageIdx = 0;

    public Player(final @NotNull ClientConnection connection, final @NotNull World world, final @NotNull GameProfile gameProfile, final @NotNull Version version) {
        super(EntityType.PLAYER, world.getWorldSpawn());

        this.gameProfile = gameProfile;
        this.connection = connection;
        this.version = version;
    }

    public @NotNull GameProfile getGameProfile() {
        return gameProfile;
    }

    public @NotNull UUID getUUID() {
        return gameProfile.getUUID();
    }

    public @NotNull String getName() {
        return gameProfile.getName();
    }

    public @NotNull ClientConnection getConnection() {
        return connection;
    }

    public void setGameMode(final @NotNull GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public @NotNull GameMode getGameMode() {
        return gameMode;
    }

    public void setPreviousGameMode(final @Nullable GameMode gameMode) {
        previousGameMode = gameMode;
    }

    public @Nullable GameMode getPreviousGameMode() {
        return previousGameMode;
    }

    public void setListed(final boolean isListed) {
        this.isListed = isListed;
    }

    public boolean isListed() {
        return isListed;
    }

    public void setFlyingSpeed(final float speed) {
        flyingSpeed = speed;
    }

    public float getFlyingSpeed() {
        return flyingSpeed;
    }

    public void setFovModifier(final float speed) {
        fovModifier = speed;
    }

    public float getFovModifier() {
        return fovModifier;
    }

    public void setViewDistance(final int chunks) {
        viewDistance = chunks;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public void setSimulationDistance(final int chunks) {
        simulationDistance = chunks;
    }

    public int getSimulationDistance() {
        return simulationDistance;
    }

    public void setPortalCooldown(final int ticks) {
        portalCooldown = ticks;
    }

    public int getPortalCooldown() {
        return portalCooldown;
    }

    public void setDeathLocation(final @Nullable Location location) {
        deathLocation = location;
    }

    public @Nullable Location getDeathLocation() {
        return deathLocation;
    }

    public @NotNull Version getVersion() {
        return version;
    }

    public @Nullable String getClientBrand() {
        return clientBrand;
    }

    public void setClientBrand(final @NotNull String brand) {
        clientBrand = brand;
    }

    public @NotNull Rank getRank() {
        return rank;
    }

    public void setRank(final @NotNull Rank rank) {
        this.rank = rank;
    }

    public void disconnect(final @NotNull String reason) {
        connection.disconnect(reason);
    }

    public void sendMessage(final @NotNull Component msg) {
        connection.sendPacket(new ClientboundSystemChatPacket(
                msg, false
        ));
    }

    public void sendActionBar(final @NotNull Component msg) {
        connection.sendPacket(new ClientboundSystemChatPacket(
                msg, true
        ));
    }

    public @Nullable ChatSession getChatSession() {
        return chatSession;
    }

    public void setChatSession(final @NotNull ChatSession chatSession) {
        this.chatSession = chatSession;
    }

    public @NotNull SignatureTracker getSignatureTracker() {
        return signatureTracker;
    }

    public int nextChatIndex() {
        return receivedMessageIdx++;
    }

    public int nextSenderIndex() {
        return sendMessageIdx++;
    }

    public enum GameMode {
        SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR;

        public boolean isInvulnerable() {
            return this == CREATIVE || this == SPECTATOR;
        }
    }

    public enum Hand {
        LEFT, RIGHT
    }
}
