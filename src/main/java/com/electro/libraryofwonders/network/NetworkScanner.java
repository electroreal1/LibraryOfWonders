package com.electro.libraryofwonders.network;

import com.electro.libraryofwonders.LibraryOfWonders;
import com.electro.libraryofwonders.network.annotation.NetworkDirection;
import com.electro.libraryofwonders.network.annotation.NetworkPacket;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = LibraryOfWonders.MODID)
public class NetworkScanner {

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        Type packetAnnotationType = Type.getType(NetworkPacket.class);

        ModList.get().getAllScanData().forEach(scanData -> {
            scanData.getAnnotations().forEach(annotationData -> {

                if (annotationData.annotationType().equals(packetAnnotationType)) {
                    try {
                        String className = annotationData.clazz().getClassName();
                        Class<?> clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());

                        if (AutoHandledPayload.class.isAssignableFrom(clazz)) {
                            registerPacket(event, clazz);
                        } else {
                            LibraryOfWonders.LOGGER.error("@NetworkPacket applied to a class that does not implement AutoHandledPayload: {}", className);
                        }

                    } catch (Exception e) {
                        LibraryOfWonders.LOGGER.error("Failed to load and register network packet: {}", annotationData.clazz().getClassName(), e);
                    }
                }
            });
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerPacket(RegisterPayloadHandlersEvent event, Class clazz) throws Exception {
        Field typeField = clazz.getDeclaredField("TYPE");
        Field codecField = clazz.getDeclaredField("STREAM_CODEC");

        CustomPacketPayload.Type type = (CustomPacketPayload.Type) typeField.get(null);
        StreamCodec codec = (StreamCodec) codecField.get(null);

        NetworkPacket annotation = (NetworkPacket) clazz.getAnnotation(NetworkPacket.class);
        NetworkDirection direction = annotation.direction();

        PayloadRegistrar registrar = event.registrar(type.id().getNamespace());

        if (direction == NetworkDirection.PLAY_TO_SERVER) {
            registrar.playToServer(type, codec, (payload, context) -> ((AutoHandledPayload) payload).handle(context));
            LibraryOfWonders.LOGGER.info("Registered C2S Packet: {}", type.id());
        } else if (direction == NetworkDirection.PLAY_TO_CLIENT) {
            registrar.playToClient(type, codec, (payload, context) -> ((AutoHandledPayload) payload).handle(context));
            LibraryOfWonders.LOGGER.info("Registered S2C Packet: {}", type.id());
        } else if (direction == NetworkDirection.BIDIRECTIONAL) {
            registrar.playBidirectional(type, codec, (payload, context) -> ((AutoHandledPayload) payload).handle(context));
            LibraryOfWonders.LOGGER.info("Registered Bidirectional Packet: {}", type.id());
        }
    }
}