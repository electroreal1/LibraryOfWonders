package com.electro.libraryofwonders.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface AutoHandledPayload extends CustomPacketPayload {
    /**
     * Called automatically when the packet is received.
     * @param context The network context containing the player, level, and task enqueueing.
     */
    void handle(IPayloadContext context);
}
