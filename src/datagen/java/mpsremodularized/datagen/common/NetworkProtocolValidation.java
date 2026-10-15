package mpsremodularized.datagen.common;

import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.network.MPSPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Exercises actual MPS registration and codecs without invoking gameplay handlers. */
final class NetworkProtocolValidation {
    private static int checks;
    private static JsonObject lastReport;

    static void validate() {
        checks = 0;
        Capture capture = new Capture();
        MPSPackets.register(new RegisterPayloadHandlersEvent() {
            @Override
            public PayloadRegistrar registrar(String version) {
                require(MPSPackets.PROTOCOL_VERSION.equals(version) && version.equals("4.2.0"), "One explicit 4.2.0 protocol");
                capture.registrars++;
                return capture;
            }
        });
        require(capture.registrars == 1, "One registration owner");
        require(capture.server == 12 && capture.client == 8 && capture.ids.size() == 20, "All directional payloads preserved plus owned native requests");
        require(mpsremodularized.common.event.PlayerUpdateHandler.shouldTickPlayer(true, true), "Local client can generate movement input");
        require(!mpsremodularized.common.event.PlayerUpdateHandler.shouldTickPlayer(true, false), "Remote clients cannot generate another player's input");
        require(mpsremodularized.common.event.PlayerUpdateHandler.shouldTickPlayer(false, false), "Every server player still ticks independently");
        PowerModule module = new PowerModule(ItemStack.EMPTY, ModuleCategory.MOVEMENT, ModuleTarget.LEGSONLY, true);
        module.addTradeoffProperty("assist", "speed", 1);
        for (double value : new double[]{0, 0.5, 1}) {
            require(MPSPackets.isAllowedTweak(module, "assist", value), "Declared slider value accepted");
        }
        for (double value : new double[]{-1, 1.1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            require(!MPSPackets.isAllowedTweak(module, "assist", value), "Malformed slider value rejected");
        }
        require(!MPSPackets.isAllowedTweak(module, MPSConstants.MOVEMENT_SPEED, 1), "Client cannot write runtime speed through a tweak");
        require(!MPSPackets.isAllowedTweak(module, "invented", 0.5), "Undeclared property rejected");
        require(mpsremodularized.common.item.module.movement.SprintAssistModule.movementEnergyCost(100, 10, true) == 600,
                "Server sprint billing retains vanilla walk-distance scale");
        require(mpsremodularized.common.item.module.movement.SprintAssistModule.movementEnergyCost(100, 10, false) == 30,
                "Server walking billing retains former 1/20 rate");
        require(mpsremodularized.common.item.module.movement.SprintAssistModule.movementEnergyCost(100, 0, true) == 0,
                "Stationary player costs no movement energy");
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -1}) {
            require(Double.isNaN(mpsremodularized.common.item.module.movement.SprintAssistModule.movementEnergyCost(invalid, 1, true)),
                    "Invalid movement cost cannot produce free speed");
        }
        lastReport = new JsonObject();
        lastReport.addProperty("protocol", MPSPackets.PROTOCOL_VERSION);
        lastReport.addProperty("required", true);
        lastReport.addProperty("thread", "MAIN");
        lastReport.addProperty("serverboundPayloads", capture.server);
        lastReport.addProperty("clientboundPayloads", capture.client);
        lastReport.addProperty("checks", checks);
        JsonObject directions = new JsonObject();
        capture.directions.forEach(directions::addProperty);
        lastReport.add("payloadDirections", directions);
        System.out.println("Network protocol and codec checks passed: " + checks);
    }

    static JsonObject report() { return lastReport.deepCopy(); }

    private static final class Capture extends PayloadRegistrar {
        final Set<ResourceLocation> ids = new HashSet<>();
        final java.util.Map<String, String> directions = new java.util.TreeMap<>();
        int server, client, registrars;
        Capture() { super(MPSPackets.PROTOCOL_VERSION); }
        @Override public PayloadRegistrar executesOn(HandlerThread thread) {
            require(thread == HandlerThread.MAIN, "Gameplay executes on the main thread");
            return this;
        }
        @Override public PayloadRegistrar optional() {
            throw new AssertionError("Essential protocol must not be optional");
        }
        @Override public <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
            server++; checkCodec(type, codec, "SERVER"); return this;
        }
        @Override public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
            client++; checkCodec(type, codec, "CLIENT"); return this;
        }
        private <T extends CustomPacketPayload> void checkCodec(CustomPacketPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec, String direction) {
            require(type.id().getNamespace().equals(MPSConstants.MOD_ID) && ids.add(type.id()), "Unique owned payload ID");
            directions.put(type.id().toString(), direction);
            try {
                Class<?> owner = Class.forName(findPayloadClass(type));
                var components = owner.getRecordComponents();
                Class<?>[] parameters = components == null ? new Class<?>[0]
                        : Arrays.stream(components).map(java.lang.reflect.RecordComponent::getType).toArray(Class<?>[]::new);
                Object[] values = Arrays.stream(parameters).map(NetworkProtocolValidation::sample).toArray();
                @SuppressWarnings("unchecked")
                T original = (T) owner.getConstructor(parameters).newInstance(values);
                RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY, net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
                RegistryFriendlyByteBuf encoded = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY, net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
                try {
                    codec.encode(buffer, original);
                    byte[] before = new byte[buffer.readableBytes()];
                    buffer.getBytes(0, before);
                    T decoded = codec.decode(buffer);
                    require(buffer.readableBytes() == 0 && decoded.type().equals(type), "Decode consumes complete payload");
                    codec.encode(encoded, decoded);
                    byte[] after = new byte[encoded.readableBytes()];
                    encoded.getBytes(0, after);
                    require(Arrays.equals(before, after), "Codec round-trip preserves fields");
                } finally { buffer.release(); encoded.release(); }
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Cannot exercise payload " + type.id(), e);
            }
        }
    }

    private static String findPayloadClass(CustomPacketPayload.Type<?> type) throws ReflectiveOperationException {
        for (String name : PAYLOAD_CLASSES) {
            Class<?> owner = Class.forName("mpsremodularized.common.network." + name);
            if (owner.getField("ID").get(null).equals(type)) return owner.getName();
        }
        throw new AssertionError("Uncatalogued payload: " + type.id());
    }

    private static final String[] PAYLOAD_CLASSES = {
        "serverbound.NativeToolRequestPacket", "clientbound.NativeWandSearchPacket",
        "serverbound.BlockPositionPacketServerBound", "serverbound.BlockStateClearPacketServerBound",
        "serverbound.ColorInfoPacketServerBound", "serverbound.CosmeticInfoPacketServerBound",
        "serverbound.ToggleRequestPacketServerBound", "serverbound.ModeChangeRequestPacketServerBound",
        "serverbound.PlayerUpdatePacketServerBound", "serverbound.TweakRequestDoublePacketServerBound",
        "clientbound.ColorInfoPacketClientBound", "clientbound.CosmeticInfoPacketClientBound",
        "clientbound.ToggleRequestPacketClientBound", "clientbound.ModeChangeRequestPacketClientBound",
        "clientbound.TweakRequestDoublePacketClientBound", "packets.serverbound.CreativeInstallPacketServerBound",
        "packets.serverbound.ContainerGuiOpenPacket", "packets.serverbound.SetSprintAssistDoubleAndDrainServerBound",
        "packets.clientbound.CreativeInstallPacketClientBound", "packets.clientbound.SetSprintAssistDoubleClientBound"
    };

    private static Object sample(Class<?> type) {
        if (type == EquipmentSlot.class) return EquipmentSlot.HEAD;
        if (type == ResourceLocation.class) return ResourceLocation.parse("mpsremodularized:night_vision");
        if (type == BlockPos.class) return new BlockPos(3, -32, 5);
        if (type == CompoundTag.class) { CompoundTag tag = new CompoundTag(); tag.putBoolean("visible", true); return tag; }
        if (type == int[].class) return new int[]{0xFF00FF00, 0x80123456};
        if (type == byte[].class) return new byte[]{0, 42, -1};
        if (type == String.class) return "assist";
        if (type == boolean.class) return true;
        if (type == int.class) return 2;
        if (type == byte.class) return (byte) 42;
        if (type == double.class) return 0.5;
        throw new AssertionError("Missing codec sample for " + type);
    }

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
