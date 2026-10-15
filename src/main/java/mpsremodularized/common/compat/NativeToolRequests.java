package mpsremodularized.common.compat;

import io.netty.buffer.Unpooled;
import java.lang.reflect.Method;
import java.util.Objects;
import mpsremodularized.common.network.serverbound.NativeToolRequestPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** A fixed native request catalog, not a general reflective RPC or replacement search/mode engine. */
public final class NativeToolRequests {
    private record Route(String donor, String type, String codec, boolean queued) {}
    private NativeToolRequests() {}
    private static Route route(ResourceLocation id) {
        String namespace = id.getNamespace(), name = id.getPath();
        if (namespace.equals("constructionstick")) {
            String type = switch (name) { case "stick_option" -> "PacketStickOption";
                case "query_undo" -> "PacketQueryUndo"; case "request_preview" -> "PacketRequestPreview"; default -> null; };
            if (type != null) return new Route("constructionstick:copper_stick,constructionstick:iron_stick,constructionstick:diamond_stick,constructionstick:netherite_stick",
                    "mrbysco.constructionstick.network." + type, "CODEC", true);
        }
        if (namespace.equals("immersiveengineering") && name.equals("be_sync"))
            return new Route("immersiveengineering:workbench", "blusunrize.immersiveengineering.common.network.MessageBlockEntitySync", "CODEC", false);
        if (namespace.equals("mffs")) {
            String type = switch (name) {
                case "set_slot_item" -> "SetItemInSlotPacket";
                case "toggle_mode" -> "ToggleModePacket";
                case "update_frequency" -> "UpdateFrequencyPacket";
                case "switch_energy_mode" -> "SwitchEnergyModePacket";
                case "switch_transfer_mode" -> "SwitchTransferModePacket";
                case "switch_confiscation_mode" -> "SwitchConfiscationModePacket";
                case "toggle_field_permission" -> "ToggleFieldPermissionPacket";
                default -> null;
            };
            if (type != null) return new Route("mffs:remote_controller", "dev.su5ed.mffs.network." + type, "STREAM_CODEC", false);
        }
        if (namespace.equals("pneumaticcraft")) {
            String type = switch (name) {
                case "change_gps_tool_coord" -> "PacketChangeGPSToolCoordinate";
                case "update_gps_area_tool" -> "PacketUpdateGPSAreaTool";
                case "update_remote_layout" -> "PacketUpdateRemoteLayout";
                case "shift_scroll_wheel" -> "PacketShiftScrollWheel";
                case "left_click_empty" -> "PacketLeftClickEmpty";
                default -> null;
            };
            if (type != null) return new Route(name.equals("update_remote_layout") ? "pneumaticcraft:remote"
                    : name.equals("update_gps_area_tool") || name.equals("left_click_empty") ? "pneumaticcraft:gps_area_tool"
                    : "pneumaticcraft:gps_tool,pneumaticcraft:gps_area_tool", "me.desht.pneumaticcraft.common.network." + type, "STREAM_CODEC", false);
        }
        if (namespace.equals("ars_additions")) {
            String type = switch (name) { case "set_advanced_wand_mode" -> "PacketUpdateAdvancedDominionWand";
                case "multi_target_connection" -> "PacketMultiTargetConnection"; default -> null; };
            if (type != null) return new Route("ars_additions:advanced_dominion_wand", "com.github.jarva.arsadditions.setup.networking." + type, "STREAM_CODEC", true);
        }
        if (namespace.equals("naturescompass") || namespace.equals("explorerscompass")) {
            String type = switch (name) { case "search" -> "SearchPacket"; case "search_for_next" -> "SearchForNextPacket"; case "teleport" -> "TeleportPacket"; default -> null; };
            if (type != null) return new Route(namespace + ":" + namespace, "com.chaosthedude." + namespace + ".network." + type, "CODEC", true);
        }
        if (namespace.equals("mekanism")) {
            String type = switch (name) { case "mode" -> "PacketModeChange"; case "radial_mode" -> "PacketRadialModeChange"; default -> null; };
            if (type != null) return new Route("mekanism:configurator", "mekanism.common.network.to_server." + type, "STREAM_CODEC", false);
            type = switch (name) {
                case "use_dropper" -> "PacketDropperUse";
                case "item_gui_interact" -> "PacketItemGuiInteract";
                case "item_button" -> "button.PacketItemButtonPress";
                case "set_item_frequency" -> "frequency.PacketSetItemFrequency";
                case "set_frequency_color" -> "frequency.PacketSetFrequencyColor";
                case "qio_take" -> "qio.PacketQIOItemViewerSlotTake";
                case "qio_shift_take" -> "qio.PacketQIOItemViewerSlotShiftTake";
                case "qio_place" -> "qio.PacketQIOItemViewerSlotPlace";
                case "clear_qio" -> "qio.PacketQIOClearCraftingWindow";
                case "fill_qio" -> "qio.PacketQIOFillCraftingWindow";
                default -> null;
            };
            if (type != null) return new Route(name.equals("use_dropper") ? "mekanism:gauge_dropper" : "mekanism:portable_qio_dashboard",
                    "mekanism.common.network.to_server." + type,"STREAM_CODEC",false);
        }
        return null;
    }
    @SuppressWarnings("unchecked") private static StreamCodec<Object, CustomPacketPayload> codec(Route route) throws ReflectiveOperationException {
        return (StreamCodec<Object, CustomPacketPayload>) Class.forName(route.type).getField(route.codec).get(null);
    }
    public static NativeToolRequestPacket wrap(String identity, CustomPacketPayload packet) {
        var route = route(packet.type().id());
        if (route == null || !packet.getClass().getName().equals(route.type)) throw new IllegalArgumentException("Unsupported native module request");
        var registries = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null
                ? net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess()
                : (net.minecraft.core.RegistryAccess)mpsremodularized.client.compat.NativeModuleRegistries.current();
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            codec(route).encode(buffer, packet); byte[] bytes = new byte[buffer.readableBytes()]; buffer.readBytes(bytes);
            return new NativeToolRequestPacket(identity, packet.type().id(), bytes);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported native request codec", error); }
        finally { buffer.release(); }
    }
    public static void sendCompass(ItemStack source, CustomPacketPayload packet, CustomPacketPayload[] extra) {
        var owner = NativeCompassIntegration.owner(source);
        if (owner == null) { PacketDistributor.sendToServer(packet, extra); return; }
        if (owner.valid(true)) PacketDistributor.sendToServer(wrap(owner.identity, packet));
    }
    public static boolean sendMode(NativeToolIntegration.Installed owner, CustomPacketPayload packet) {
        if (owner == null) { PacketDistributor.sendToServer(packet); return true; }
        if (owner.valid()) PacketDistributor.sendToServer(wrap(owner.identity(), packet));
        return true;
    }
    public static boolean handle(NativeToolRequestPacket request, IPayloadContext context) {
        var route = route(request.nativeType());
        if (route == null || !context.flow().isServerbound()) return false;
        var installed = request.nativeType().getNamespace().equals("immersiveengineering")
                && context.player().containerMenu instanceof mpsremodularized.imixin.common.item.INativeToolMenu menu
                ? menu.mpsremodularized$nativeOwner() : NativeToolIntegration.find(context.player(), InteractionHand.MAIN_HAND, null);
        if (installed == null || !java.util.Arrays.asList(route.donor.split(",")).contains(installed.item().donor.toString())
                || !installed.valid() || !Objects.equals(installed.identity(), request.identity())) return false;
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(request.data()),context.player().registryAccess());
        try {
            var packet = codec(route).decode(buffer);
            if (buffer.isReadable() || !packet.type().id().equals(request.nativeType())) return false;
            if (request.nativeType().getNamespace().equals("immersiveengineering")) return NativeStationIntegration.configureWorkbench(installed, packet);
            if (java.util.Set.of("pneumaticcraft", "ars_additions", "mffs").contains(request.nativeType().getNamespace())) {
                if (!RemainingNativePolicies.allowed(installed, packet)) return false;
            }
            if (route.donor.equals("mekanism:configurator") && packet.getClass().getMethod("slot").invoke(packet) != net.minecraft.world.entity.EquipmentSlot.MAINHAND) return false;
            if (route.donor.equals("mekanism:gauge_dropper") && !MekanismDropperIntegration.allowed(context.player(),packet)) return false;
            if (route.donor.equals("mekanism:portable_qio_dashboard")) {
                if (!(context.player().containerMenu instanceof mpsremodularized.imixin.common.item.INativeToolMenu menu)
                        || menu.mpsremodularized$nativeOwner()==null || !menu.mpsremodularized$nativeOwnerValid()) return false;
                String hand = switch(request.nativeType().getPath()) {case "set_item_frequency" -> "currentHand";case "item_gui_interact", "item_button" -> "hand";default -> null;};
                if(hand!=null && packet.getClass().getMethod(hand).invoke(packet)!=InteractionHand.MAIN_HAND)return false;
            }
            return NativeToolIntegration.use(installed, () -> {
                try {
                    if (route.queued) {
                        // The MPS registrar already runs on MAIN. Enter the native queued work directly.
                        var handler = request.nativeType().getNamespace().equals("constructionstick") ? Class.forName(route.type + "$Handler") : packet.getClass();
                        Method work = java.util.Arrays.stream(handler.getDeclaredMethods())
                                .filter(method -> route.donor.startsWith("ars_additions:") ? method.getName().startsWith("lambda$handleData$") : method.getName().equals("lambda$handle$0"))
                                .filter(method -> java.util.Arrays.asList(method.getParameterTypes()).contains(IPayloadContext.class)).findFirst().orElseThrow();
                        work.setAccessible(true);
                        Object[] arguments = java.util.Arrays.stream(work.getParameterTypes()).map(type -> type == IPayloadContext.class ? context : packet).toArray();
                        work.invoke(java.lang.reflect.Modifier.isStatic(work.getModifiers()) ? null : packet, arguments);
                    } else if (request.nativeType().getNamespace().equals("pneumaticcraft"))
                        packet.getClass().getMethod("handle", packet.getClass(), IPayloadContext.class).invoke(null, packet, context);
                    else packet.getClass().getMethod("handle", IPayloadContext.class).invoke(packet, context);
                    return true;
                } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported native request handler", error); }
            });
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported native request codec", error); }
        finally { buffer.release(); }
    }

    public static boolean supportsQio(CustomPacketPayload packet) {
        var route=route(packet.type().id());return route!=null&&route.donor.equals("mekanism:portable_qio_dashboard")&&route.type.equals(packet.getClass().getName());
    }
    public static boolean supportsRemaining(CustomPacketPayload packet) {
        var route = route(packet.type().id());
        return route != null && route.type.equals(packet.getClass().getName())
                && (route.donor.startsWith("constructionstick:") || route.donor.startsWith("pneumaticcraft:") || route.donor.startsWith("ars_additions:") || route.donor.equals("mffs:remote_controller"));
    }
    public static boolean supportsRemaining(NativeToolIntegration.Installed owner, CustomPacketPayload packet) {
        var route = route(packet.type().id());
        return supportsRemaining(packet) && java.util.Arrays.asList(route.donor.split(",")).contains(owner.item().donor.toString());
    }
}
