package net.gemcraft.hub.protocol.client;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ChatSession(@NotNull UUID sessionId, long expiresAt, byte[] publicKey, byte[] keySignature) {}
