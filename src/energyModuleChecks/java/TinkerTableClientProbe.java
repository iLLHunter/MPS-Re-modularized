import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Optional Java agent for a disposable packaged-client world; never packaged in the mod. */
public final class TinkerTableClientProbe {
    static ClassLoader loader;
    static Path output;
    static int checks;
    static Object tablePos;
    static Class<?> type(String name) throws Exception { return Class.forName(name, true, loader); }
    static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        return target.getClass().getMethod(name, types).invoke(target, args);
    }
    static Object field(Object object, String name) throws Exception {
        for (Class<?> owner = object.getClass(); owner != null; owner = owner.getSuperclass()) {
            try { Field field = owner.getDeclaredField(name); field.setAccessible(true); return field.get(object); }
            catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(name);
    }
    static void report(String text) throws Exception {
        Files.writeString(output.resolve("probe.txt"), text + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        System.out.println("TINKER CLIENT " + text);
    }
    static void require(boolean condition, String message) throws Exception {
        if (!condition) throw new AssertionError(message);
        checks++; report("PASS " + message);
    }
    interface Work { void run() throws Exception; }
    static void task(Object target, Work work) throws Exception {
        CompletableFuture<Void> done = new CompletableFuture<>();
        call(target, "execute", new Class<?>[]{Runnable.class}, (Runnable) () -> {
            try { work.run(); done.complete(null); } catch (Throwable error) { done.completeExceptionally(error); }
        });
        done.get(30, TimeUnit.SECONDS);
    }
    static Object slot(String name) throws Exception { return type("net.minecraft.world.entity.EquipmentSlot").getField(name).get(null); }
    static Object item(String id) throws Exception {
        Object registry = type("net.minecraft.core.registries.BuiltInRegistries").getField("ITEM").get(null);
        Object location = type("net.minecraft.resources.ResourceLocation").getMethod("parse", String.class).invoke(null, id);
        return call(registry, "get", new Class<?>[]{type("net.minecraft.resources.ResourceLocation")}, location);
    }
    static Object stack(String id) throws Exception { return type("net.minecraft.world.item.ItemStack").getConstructor(type("net.minecraft.world.level.ItemLike")).newInstance(item(id)); }
    static Object host(Object stack) throws Exception { return type("mpsremodularized.common.registration.MPSCapabilities").getMethod("getModularItemOrModeChangingCapability", type("net.minecraft.world.item.ItemStack")).invoke(null, stack); }
    static Object player(Object server) throws Exception { return ((List<?>) call(call(server, "getPlayerList", new Class<?>[0]), "getPlayers", new Class<?>[0])).getFirst(); }
    static Object equipment(Object player, String slot) throws Exception { return call(player, "getItemBySlot", new Class<?>[]{type("net.minecraft.world.entity.EquipmentSlot")}, slot(slot)); }
    static List<?> buttons(Object frame) throws Exception {
        List<Object> result = new ArrayList<>();
        for (Object category : ((Map<?, ?>) field(frame, "categories")).values()) result.addAll((List<?>) field(category, "moduleButtons"));
        return result;
    }
    static String id(Object button) throws Exception { return call(button, "getRegName", new Class<?>[0]).toString(); }
    static double coordinate(Object rectangle, String name) throws Exception { return ((Number) call(rectangle, name, new Class<?>[0])).doubleValue(); }
    static void select(Object frame, String id) throws Exception {
        Object button = null;
        for (Object candidate : buttons(frame)) if (id(candidate).equals(id)) button = candidate;
        require(button != null, "Visible module choice " + id);
        double scroll = Math.max(0, coordinate(button, "centerY") - coordinate(frame, "top") - 50);
        call(frame, "setCurrentScrollPixels", new Class<?>[]{double.class}, scroll);
        scroll = coordinate(frame, "getCurrentScrollPixels");
        call(field(frame, "scrollBar"), "setValue", new Class<?>[]{double.class}, scroll);
        call(frame, "mouseClicked", new Class<?>[]{double.class, double.class, int.class}, coordinate(button, "centerX"), coordinate(button, "centerY") - scroll, 0);
        require(id.equals(id(call(frame, "getSelectedModule", new Class<?>[0]))), "Mouse selection resolves " + id);
    }
    static void press(Object button) throws Exception {
        call(button, "mouseClicked", new Class<?>[]{double.class, double.class, int.class}, coordinate(button, "centerX"), coordinate(button, "centerY"), 0);
    }
    static void pressOnScreen(Object screen, Object button) throws Exception {
        require((Boolean) call(screen, "mouseClicked", new Class<?>[]{double.class, double.class, int.class},
                coordinate(button, "centerX"), coordinate(button, "centerY"), 0), "Creative action is handled through the screen mouse input");
    }
    static void tabsFit(Object screen) throws Exception {
        require(coordinate(field(screen, "tabSelectFrame"), "top") >= 0
                && coordinate(screen, "bottom") <= ((Number) field(screen, "height")).intValue(),
                "Header and panel fit the short window on " + screen.getClass().getSimpleName());
    }
    static boolean withoutJei() { return Boolean.getBoolean("mps.tinkerProbe.withoutJei"); }
    static void validateJei(Object screen) throws Exception {
        if (withoutJei()) {
            Object mods = type("net.neoforged.fml.ModList").getMethod("get").invoke(null);
            require(!(Boolean) call(mods, "isLoaded", new Class<?>[]{String.class}, "jei"), "Tinker Table opens with JEI absent");
            return;
        }
        Object runtime = type("mezz.jei.common.Internal").getMethod("getJeiRuntime").invoke(null);
        Object toggle = type("mezz.jei.common.Internal").getMethod("getClientToggleState").invoke(null);
        require(runtime != null && (Boolean) call(toggle, "isOverlayEnabled", new Class<?>[0]), "JEI remains enabled on the Tinker Table");
        Object sidebar = call(screen, "getJeiSidebarArea", new Class<?>[0]);
        Object helper = call(runtime, "getScreenHelper", new Class<?>[0]);
        var areas = (java.util.stream.Stream<?>) call(helper, "getGuiExclusionAreas", new Class<?>[]{type("net.minecraft.client.gui.screens.Screen")}, screen);
        boolean reserved = false;
        for (Object area : areas.toList()) if (coordinate(area, "getX") == coordinate(sidebar, "getX")
                && coordinate(area, "getY") == coordinate(sidebar, "getY") && coordinate(area, "getWidth") == coordinate(sidebar, "getWidth")
                && coordinate(area, "getHeight") == coordinate(sidebar, "getHeight")) reserved = true;
        require(reserved, "JEI receives the Tinker equipment and Creative-button exclusion area");
        Object bookmarks = call(runtime, "getBookmarkOverlay", new Class<?>[0]);
        for (String name : List.of("bookmarkButton", "historyButton")) {
            Object area = call(field(bookmarks, name), "getArea", new Class<?>[0]);
            double x = coordinate(area, "x"), y = coordinate(area, "y"), w = coordinate(area, "width"), h = coordinate(area, "height");
            double sx = coordinate(sidebar, "getX"), sy = coordinate(sidebar, "getY"), sw = coordinate(sidebar, "getWidth"), sh = coordinate(sidebar, "getHeight");
            require(x >= 0 && y >= 0 && x + w <= ((Number) field(screen, "width")).intValue()
                    && y + h <= ((Number) field(screen, "height")).intValue()
                    && !(x < sx + sw && x + w > sx && y < sy + sh && y + h > sy), "JEI " + name + " remains onscreen and clear of Tinker controls");
        }
    }
    static void screenshot(Object client, String name) throws Exception {
        Object target = call(client, "getMainRenderTarget", new Class<?>[0]);
        Object image = type("net.minecraft.client.Screenshot").getMethod("takeScreenshot", type("com.mojang.blaze3d.pipeline.RenderTarget")).invoke(null, target);
        try { call(image, "writeToFile", new Class<?>[]{Path.class}, output.resolve(name + ".png")); }
        finally { call(image, "close", new Class<?>[0]); }
        report("SCREENSHOT " + name);
    }
    static void reopen(Object server, String equipmentSlot) throws Exception {
        Object player = player(server);
        Object result = type("mpsremodularized.common.container.InstallationAccess").getMethod("reopen", type("net.minecraft.server.level.ServerPlayer"), type("net.minecraft.world.entity.EquipmentSlot"), boolean.class, double.class, double.class)
                .invoke(null, player, slot(equipmentSlot), false, -1d, -1d);
        require((Boolean) result, "Server reopens " + equipmentSlot + " through placed-table access");
    }
    static String nativeId(String donor) throws Exception {
        for (Object entry : (List<?>) type("mpsremodularized.common.compat.IntermodModules").getField("ENTRIES").get(null)) {
            if (call(entry, "donor", new Class<?>[0]).equals(donor)) {
                Object holder = call(entry, "item", new Class<?>[0]);
                return call(holder, "getId", new Class<?>[0]).toString();
            }
        }
        throw new AssertionError("No native entry " + donor);
    }
    static void setup(Object server) throws Exception {
        Object player = player(server);
        call(player, "setGameMode", new Class<?>[]{type("net.minecraft.world.level.GameType")}, type("net.minecraft.world.level.GameType").getField("CREATIVE").get(null));
        String[] slots = {"MAINHAND", "OFFHAND", "HEAD", "CHEST", "LEGS", "FEET"};
        String[] ids = {"powerfist", "powerfist", "power_armor_helmet", "power_armor_chestplate", "power_armor_leggings", "power_armor_boots"};
        for (int i = 0; i < slots.length; i++) call(player, "setItemSlot", new Class<?>[]{type("net.minecraft.world.entity.EquipmentSlot"), type("net.minecraft.world.item.ItemStack")}, slot(slots[i]), stack("mpsremodularized:" + ids[i]));
        Object pos = call(call(player, "blockPosition", new Class<?>[0]), "below", new Class<?>[0]);
        tablePos = pos;
        Object level = call(player, "level", new Class<?>[0]);
        Object table = call(type("mpsremodularized.common.registration.MPSBlocks").getField("TINKER_TABLE_BLOCK").get(null), "get", new Class<?>[0]);
        call(level, "setBlockAndUpdate", new Class<?>[]{type("net.minecraft.core.BlockPos"), type("net.minecraft.world.level.block.state.BlockState")}, pos, call(table, "defaultBlockState", new Class<?>[0]));
        Object access = type("mpsremodularized.common.container.InstallationAccess").getMethod("grantPlacedTable", type("net.minecraft.world.entity.player.Player"), type("net.minecraft.world.level.Level"), type("net.minecraft.core.BlockPos")).invoke(null, player, level, pos);
        require(access != null, "Placed table grants live integrated-server access");
    }
    static void run(Object client) throws Exception {
        task(client, () -> { Object options = field(client, "options"); options.getClass().getField("pauseOnLostFocus").setBoolean(options, false); call(client, "setScreen", new Class<?>[]{type("net.minecraft.client.gui.screens.Screen")}, (Object) null); });
        Object server = call(client, "getSingleplayerServer", new Class<?>[0]);
        task(server, () -> setup(server)); Thread.sleep(1500);
        task(client, () -> {
            Object hitPosition = type("net.minecraft.world.phys.Vec3").getMethod("atCenterOf", type("net.minecraft.core.Vec3i")).invoke(null, tablePos);
            Object hit = type("net.minecraft.world.phys.BlockHitResult").getConstructor(type("net.minecraft.world.phys.Vec3"), type("net.minecraft.core.Direction"), type("net.minecraft.core.BlockPos"), boolean.class)
                    .newInstance(hitPosition, type("net.minecraft.core.Direction").getField("UP").get(null), tablePos, false);
            call(field(client, "gameMode"), "useItemOn", new Class<?>[]{type("net.minecraft.client.player.LocalPlayer"), type("net.minecraft.world.InteractionHand"), type("net.minecraft.world.phys.BlockHitResult")},
                    field(client, "player"), type("net.minecraft.world.InteractionHand").getField("MAIN_HAND").get(null), hit);
        }); Thread.sleep(1500);
        String stick = nativeId("constructionstick:netherite_stick");
        String anvil = nativeId("cyclic:anvil");
        task(client, () -> {
            Object screen = field(client, "screen");
            require(screen.getClass().getName().endsWith("InstallSalvageGui"), "Real Install/Salvage screen opens from server menu");
            for (Object child : (List<?>) call(screen, "children", new Class<?>[0])) {
                try { report("WIDGET " + child.getClass().getName() + " " + call(child, "getX", new Class<?>[0]) + "," + call(child, "getY", new Class<?>[0])
                        + " " + call(child, "getWidth", new Class<?>[0]) + "x" + call(child, "getHeight", new Class<?>[0])); }
                catch (NoSuchMethodException ignored) {}
            }
            tabsFit(screen);
            validateJei(screen);
            Object frame = field(screen, "moduleSelectFrame");
            Object selection = field(screen, "itemSelectFrame");
            Object install = call(selection, "getCreativeInstallButton", new Class<?>[0]);
            Object all = call(selection, "getCreativeInstallAllButton", new Class<?>[0]);
            require(coordinate(install, "height") == 12 && coordinate(all, "height") == 12
                    && call(call(all, "getLabel", new Class<?>[0]), "getString", new Class<?>[0]).equals("All")
                    && coordinate(install, "left") >= 0 && coordinate(all, "bottom") <= ((Number) field(screen, "height")).intValue()
                    && coordinate(install, "right") <= coordinate(frame, "left") && coordinate(all, "right") <= coordinate(frame, "left"),
                    "Compact stacked Creative buttons fit the short window without overlapping the module list");
            require(!((List<?>) call(frame, "getPossibleItems", new Class<?>[0])).isEmpty(), "Constructor retains Creative Install All catalog");
            select(frame, stick);
            Object old = call(frame, "getSelectedModule", new Class<?>[0]);
            call(frame, "loadModules", new Class<?>[]{boolean.class}, true);
            require(call(frame, "getSelectedModule", new Class<?>[0]) != old && id(call(frame, "getSelectedModule", new Class<?>[0])).equals(stick), "Reload retains selection on a fresh live button");
            int width = ((Number) field(screen, "width")).intValue(), height = ((Number) field(screen, "height")).intValue();
            call(screen, "init", new Class<?>[]{type("net.minecraft.client.Minecraft"), int.class, int.class}, client, width + 80, height + 100);
            Object fullSelection = field(screen, "itemSelectFrame");
            Object fullAll = call(fullSelection, "getCreativeInstallAllButton", new Class<?>[0]);
            require(coordinate(fullAll, "height") == 18 && call(call(fullAll, "getLabel", new Class<?>[0]), "getString", new Class<?>[0]).equals("Install All"),
                    "Taller windows retain the original Creative button layout");
            call(screen, "init", new Class<?>[]{type("net.minecraft.client.Minecraft"), int.class, int.class}, client, width - 16, height - 16);
            frame = field(screen, "moduleSelectFrame");
            require(id(call(frame, "getSelectedModule", new Class<?>[0])).equals(stick), "Resize retains module selection");
            call(screen, "init", new Class<?>[]{type("net.minecraft.client.Minecraft"), int.class, int.class}, client, width, height);
            frame = field(screen, "moduleSelectFrame");
            call(frame, "loadModules", new Class<?>[]{boolean.class}, false);
            require(call(frame, "getSelectedModule", new Class<?>[0]) == null, "Fresh catalog clears stale selection");
            call(screen, "tick", new Class<?>[0]);
            pressOnScreen(screen, call(field(screen, "itemSelectFrame"), "getCreativeInstallAllButton", new Class<?>[0]));
        });
        Thread.sleep(2000);
        task(server, () -> {
            Object cap = host(equipment(player(server), "MAINHAND"));
            Set<String> installed = new HashSet<>(); for (Object name : (List<?>) call(cap, "getInstalledModuleNames", new Class<?>[0])) installed.add(name.toString());
            require(installed.contains(stick), "Install All network action installs augmented Netherite Construction Stick");
            require(installed.contains(anvil), "Install All network action includes Powered Diamond Anvil");
            require(installed.contains(nativeId("minecraft:anvil")), "Install All includes MkII Anvil");
        });
        task(client, () -> screenshot(client, "fist-install-all"));
        task(client, () -> {
            Object screen = field(client, "screen");
            Object grid = field(screen, "modularItemInventory");
            List<?> indexes = (List<?>) field(grid, "slotIndexes");
            require(indexes.size() == 73, "Install/Salvage exposes all 73 Fist module slots");
            Object shift = call(grid, "getULShift", new Class<?>[0]);
            double x = coordinate(grid, "right") + coordinate(shift, "x") - 4;
            double y = coordinate(grid, "centerY") + coordinate(shift, "y");
            require((Boolean) call(screen, "mouseScrolled", new Class<?>[]{double.class, double.class, double.class, double.class}, x, y, 0d, -100d),
                    "Mouse wheel at the rendered inventory right edge is handled");
            report("PAGE screen=" + field(screen, "width") + "x" + field(screen, "height") + " coords=" + x + "," + y
                    + " shift=" + coordinate(shift, "x") + "," + coordinate(shift, "y") + " scroll=" + field(grid, "currentScroll")
                    + " limit=" + field(grid, "scrollLimit"));
            call(grid, "update", new Class<?>[]{double.class, double.class}, x, y);
            Object menu = call(screen, "getMenu", new Class<?>[0]);
            Object last = call(menu, "getSlot", new Class<?>[]{int.class}, indexes.getLast());
            Object stickSlot = call(menu, "getSlot", new Class<?>[]{int.class}, indexes.get(6));
            report("PAGE updated=" + field(grid, "currentScroll") + " last=" + indexes.getLast() + ":" + call(last, "isActive", new Class<?>[0])
                    + " stick=" + indexes.get(6) + ":" + call(stickSlot, "isActive", new Class<?>[0]));
            require((Boolean) call(last, "isActive", new Class<?>[0]) && !(Boolean) call(stickSlot, "isActive", new Class<?>[0]),
                    "The last Fist slot is reachable and offscreen dedicated slots are disabled");
        }); Thread.sleep(600);
        task(client, () -> screenshot(client, "fist-final-page"));
        if (withoutJei()) {
            report("COMPLETE checks=" + checks);
            return;
        }
        for (String[] piece : new String[][]{{"HEAD", nativeId("secretroomsmod:true_vision_goggles")}, {"CHEST", "mpsremodularized:block_reach"}}) {
            task(server, () -> reopen(server, piece[0])); Thread.sleep(1200);
            task(client, () -> { Object screen = field(client, "screen"); Object frame = field(screen, "moduleSelectFrame"); select(frame, piece[1]); pressOnScreen(screen, call(field(screen, "itemSelectFrame"), "getCreativeInstallButton", new Class<?>[0])); });
            Thread.sleep(1200);
            task(server, () -> require(((List<?>) call(host(equipment(player(server), piece[0])), "getInstalledModuleNames", new Class<?>[0])).stream().anyMatch(name -> name.toString().equals(piece[1])), "Selected Creative Install reaches server for " + piece[1]));
            task(client, () -> screenshot(client, piece[0].toLowerCase() + "-install"));
        }
        task(client, () -> {
            Object title = type("net.minecraft.network.chat.Component").getMethod("literal", String.class).invoke(null, "Tinker validation");
            Object screen = type("mpsremodularized.client.gui.module.tweak.ModuleTweakGui").getConstructor(type("net.minecraft.network.chat.Component")).newInstance(title);
            call(client, "setScreen", new Class<?>[]{type("net.minecraft.client.gui.screens.Screen")}, screen);
            tabsFit(screen);
            Object selector = field(screen, "itemSelectFrame");
            for (Object box : (List<?>) field(selector, "boxes")) if (box.getClass().getName().endsWith("ModularItemTabToggleWidget") && call(box, "getSlotType", new Class<?>[0]).equals(slot("CHEST"))) press(box);
            select(field(screen, "moduleSelectFrame"), "mpsremodularized:block_reach");
            Object tweaks = field(screen, "tweakFrame"); call(tweaks, "update", new Class<?>[]{double.class, double.class}, 0d, 0d);
            List<?> sliders = (List<?>) field(tweaks, "sliders"); require(sliders.size() == 1, "Block Reach slider appears on Module Tweak tab");
            Object label = type("net.minecraft.network.chat.Component").getMethod("translatable", String.class).invoke(null, "module.tradeoff.Block Reach");
            require(call(label, "getString", new Class<?>[0]).equals("Block Reach"), "Block Reach slider and property labels are translated");
            require(((Number) ((Map<?, ?>) field(tweaks, "propertyStrings")).get("Block Reach")).doubleValue() == 6,
                    "Tweak display shows Creative vanilla reach plus one at minimum");
            Object slider = sliders.getFirst(); press(slider); call(slider, "setValueByMouse", new Class<?>[]{double.class}, coordinate(slider, "right")); call(slider, "mouseReleased", new Class<?>[]{double.class, double.class, int.class}, coordinate(slider, "right"), coordinate(slider, "centerY"), 0);
            call(tweaks, "setTotalSize", new Class<?>[]{int.class}, 500); call(field(tweaks, "scrollBar"), "setMaxValue", new Class<?>[]{double.class}, coordinate(tweaks, "getMaxScrollPixels"));
            call(tweaks, "mouseScrolled", new Class<?>[]{double.class, double.class, double.class}, coordinate(tweaks, "centerX"), coordinate(tweaks, "centerY"), -3d);
            double scrolled = coordinate(tweaks, "getCurrentScrollPixels"); call(tweaks, "update", new Class<?>[]{double.class, double.class}, 0d, 0d);
            require(scrolled > 0 && coordinate(tweaks, "getCurrentScrollPixels") == scrolled, "Tweak wheel position survives the next update");
            call(tweaks, "resetScroll", new Class<?>[0]); call(tweaks, "update", new Class<?>[]{double.class, double.class}, 0d, 0d); require(coordinate(tweaks, "getCurrentScrollPixels") == 0, "Tweak reset clears scrollbar position");
            int width = ((Number) field(screen, "width")).intValue(), height = ((Number) field(screen, "height")).intValue();
            call(screen, "init", new Class<?>[]{type("net.minecraft.client.Minecraft"), int.class, int.class}, client, width - 16, height - 16);
            require(id(call(field(screen, "moduleSelectFrame"), "getSelectedModule", new Class<?>[0])).equals("mpsremodularized:block_reach"), "Tweak resize retains Block Reach selection");
            call(screen, "init", new Class<?>[]{type("net.minecraft.client.Minecraft"), int.class, int.class}, client, width, height);
        }); Thread.sleep(1500);
        task(server, () -> { Object cap = host(equipment(player(server), "CHEST")); Object location = type("net.minecraft.resources.ResourceLocation").getMethod("parse", String.class).invoke(null, "mpsremodularized:block_reach"); require(((Number) call(cap, "getModuleDouble", new Class<?>[]{type("net.minecraft.resources.ResourceLocation"), String.class}, location, "Block Reach")).doubleValue() == 1, "Block Reach slider change reaches server and saves max setting"); });
        task(client, () -> {
            Object tweaks = field(field(client, "screen"), "tweakFrame");
            require(((Number) ((Map<?, ?>) field(tweaks, "propertyStrings")).get("Block Reach")).doubleValue() == 59,
                    "Tweak display shows the saved 59-block maximum");
            screenshot(client, "block-reach-tweak");
        });
        task(client, () -> {
            Object title = type("net.minecraft.network.chat.Component").getMethod("literal", String.class).invoke(null, "Keybind validation");
            Object screen = type("mpsremodularized.client.gui.keymap.TinkerKeymapGui").getConstructor(type("net.minecraft.world.entity.player.Player"), type("net.minecraft.network.chat.Component")).newInstance(field(client, "player"), title);
            call(client, "setScreen", new Class<?>[]{type("net.minecraft.client.gui.screens.Screen")}, screen);
            tabsFit(screen);
            long tabs = ((List<?>) field(screen, "renderables")).stream().filter(frame -> frame.getClass().getName().endsWith("TabSelectFrame")).count();
            require(tabs == 1, "Keybind tab controls are registered once");
            Object frame = field(screen, "kbFrame");
            int height = ((Number) call(frame, "getTotalSize", new Class<?>[0])).intValue();
            call(frame, "loadConditions", new Class<?>[0]);
            require(((Number) call(frame, "getTotalSize", new Class<?>[0])).intValue() == height, "Reloading keybinds keeps the correct scroll height");
            List<?> rows = (List<?>) field(frame, "keyBindSubFrames");
            Set<String> names = new HashSet<>();
            for (Object row : rows) names.add(field(field(row, "kb"), "registryName").toString());
            require(names.contains("mpsremodularized:block_reach") && names.contains(nativeId("secretroomsmod:true_vision_goggles")), "New modules have entries on the keybind tab");
            Object hiddenButton = field(rows.getLast(), "button");
            require(coordinate(hiddenButton, "centerY") > coordinate(frame, "bottom"), "Regression fixture includes an offscreen keybind row");
            require(!(Boolean) call(frame, "mouseClicked", new Class<?>[]{double.class, double.class, int.class}, coordinate(hiddenButton, "centerX"), coordinate(hiddenButton, "centerY"), 0)
                    && field(frame, "keybindingToRemap") == null, "Clicking outside the keybind viewport cannot edit a hidden row");
        }); Thread.sleep(600);
        task(client, () -> screenshot(client, "keybinds"));
        task(client, () -> {
            Object title = type("net.minecraft.network.chat.Component").getMethod("literal", String.class).invoke(null, "Visual validation");
            Object inventory = call(field(client, "player"), "getInventory", new Class<?>[0]);
            Object screen = type("mpsremodularized.client.gui.cosmetic.CosmeticGui").getConstructor(type("net.minecraft.world.entity.player.Inventory"), type("net.minecraft.network.chat.Component")).newInstance(inventory, title);
            call(client, "setScreen", new Class<?>[]{type("net.minecraft.client.gui.screens.Screen")}, screen);
            tabsFit(screen);
        }); Thread.sleep(600);
        task(client, () -> screenshot(client, "visual"));
        if (!withoutJei()) {
            task(client, () -> {
                Object runtime = type("mezz.jei.common.Internal").getMethod("getJeiRuntime").invoke(null);
                Object ingredients = call(runtime, "getIngredientManager", new Class<?>[0]);
                Object itemType = type("mezz.jei.api.constants.VanillaTypes").getField("ITEM_STACK").get(null);
                Optional<?> ingredient = (Optional<?>) call(ingredients, "createTypedIngredient", new Class<?>[]{type("mezz.jei.api.ingredients.IIngredientType"), Object.class}, itemType, stack("mpsremodularized:block_reach"));
                require(ingredient.isPresent(), "JEI accepts the Block Reach module for recipe lookup");
                Object recipes = call(runtime, "getRecipesGui", new Class<?>[0]);
                Object factory = call(call(runtime, "getJeiHelpers", new Class<?>[0]), "getFocusFactory", new Class<?>[0]);
                Object role = type("mezz.jei.api.recipe.RecipeIngredientRole").getField("OUTPUT").get(null);
                Object focus = call(factory, "createFocus", new Class<?>[]{type("mezz.jei.api.recipe.RecipeIngredientRole"), type("mezz.jei.api.ingredients.ITypedIngredient")}, role, ingredient.orElseThrow());
                call(recipes, "show", new Class<?>[]{List.class}, List.of(focus));
                require((Boolean) call(recipes, "isOpen", new Class<?>[0]), "JEI opens the Block Reach crafting recipe");
            }); Thread.sleep(600);
            task(client, () -> { screenshot(client, "jei-block-reach-recipe"); call(field(client, "screen"), "onClose", new Class<?>[0]); });
        }
        report("COMPLETE checks=" + checks);
    }
    public static void premain(String directory, Instrumentation instrumentation) throws Exception {
        output = Path.of(directory); Files.createDirectories(output);
        if (!Path.of("").toAbsolutePath().normalize().equals(output.resolve("profile").toAbsolutePath().normalize()))
            throw new IllegalArgumentException("Client probe requires its own disposable profile");
        Thread worker = new Thread(() -> {
            Object client = null;
            try {
                for (int attempt = 0; attempt < 360 && client == null; attempt++) {
                    if (loader == null) for (Class<?> candidate : instrumentation.getAllLoadedClasses()) if (candidate.getName().equals("net.minecraft.client.Minecraft")) loader = candidate.getClassLoader();
                    if (loader != null) {
                        Object candidate = type("net.minecraft.client.Minecraft").getMethod("getInstance").invoke(null);
                        if (candidate != null && field(candidate, "player") != null && field(candidate, "level") != null) client = candidate;
                        else if (candidate != null && attempt % 40 == 0) task(candidate, () -> {
                            Object screen = field(candidate, "screen");
                            report("WAIT SCREEN " + (screen == null ? "null" : screen.getClass().getName()));
                            if (screen != null) {
                                for (Object child : (List<?>) call(screen, "children", new Class<?>[0])) try {
                                    Object message = call(child, "getMessage", new Class<?>[0]);
                                    String text = call(message, "getString", new Class<?>[0]).toString();
                                    report("WAIT WIDGET " + text);
                                    if (screen.getClass().getName().endsWith("ConfirmExperimentalFeaturesScreen") && text.equals("Proceed")) call(child, "onPress", new Class<?>[0]);
                                    if (screen.getClass().getName().endsWith("AccessibilityOnboardingScreen") && text.equals("Continue")) call(child, "onPress", new Class<?>[0]);
                                } catch (NoSuchMethodException ignored) {}
                            }
                            screenshot(candidate, "waiting");
                        });
                    }
                    if (client == null) Thread.sleep(500);
                }
                if (client == null) throw new AssertionError("Disposable client world did not join");
                run(client);
            } catch (Throwable error) {
                error.printStackTrace(); try { report("FAIL " + error); } catch (Exception ignored) {}
            } finally {
                if (client != null) try { Object ownedClient = client; task(client, () -> call(ownedClient, "stop", new Class<?>[0])); } catch (Exception ignored) {}
            }
        }, "Disposable Tinker Table client checks"); worker.setDaemon(true); worker.start();
    }
}
