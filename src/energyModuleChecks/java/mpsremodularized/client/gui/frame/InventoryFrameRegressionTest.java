package mpsremodularized.client.gui.frame;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.IntStream;
import mpsremodularized.client.gui.geometry.MusePoint2D;
import mpsremodularized.client.gui.geometry.Rect;
import mpsremodularized.common.container.slot.IHideableSlot;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.LoadingModList;
import mpsremodularized.client.gui.clickable.slider.VanillaFrameScrollBar;

public final class InventoryFrameRegressionTest {
    private static int checks;

    public static void main(String[] args) {
        LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        TestMenu menu = new TestMenu(61);
        List<Integer> indexes = IntStream.range(0, 61).boxed().toList();
        InventoryFrame frame = new InventoryFrame(new Rect(178, 14, 353, 118), menu, 9, indexes, null) {};
        check(frame.tiles.size() == 54, "The existing first page contains six rows");
        check(!menu.getSlot(60).isActive(), "The last slot starts outside the viewport");

        // A later page must index its tiles locally, rather than using saved inventory indices.
        frame.currentScroll = 1;
        frame.loadSlots();
        check(frame.tiles.size() == 52, "The second page includes the partial final row");
        check(frame.tiles.getFirst().slot == 9 && frame.tiles.getLast().slot == 60,
                "All final-tier slots can be reached");
        check(menu.getSlot(60).isActive() && menu.getSlot(60).getItem().is(Items.DIAMOND),
                "The installed item is visible in its real slot");
        check(!menu.getSlot(0).isActive() && menu.getSlot(0).x == -1000,
                "Offscreen slots cannot overlap the displayed page");
        check(menu.getSlot(9).x == 178 && menu.getSlot(9).y == 14,
                "The scrolled page starts at the same grid origin");
        check(menu.getSlot(60).x == 286 && menu.getSlot(60).y == 104,
                "The last item occupies the final visible row");

        check(frame.mouseScrolled(200, 30, 1), "Wheel up is handled inside the module grid");
        frame.update(200, 30);
        check(frame.currentScroll == 0 && menu.getSlot(0).isActive() && !menu.getSlot(60).isActive(),
                "Wheel up returns to the first page and hides the previous last row");
        frame.mouseScrolled(200, 30, -1);
        frame.update(200, 30);
        check(frame.currentScroll == 1 && menu.getSlot(60).isActive(),
                "Wheel down reveals the final slot");
        check(!frame.mouseScrolled(0, 0, -1), "Scrolling outside the grid leaves it unchanged");
        frame.mouseScrolled(200, 30, -100);
        frame.update(200, 30);
        check(frame.currentScroll == 1, "Scrolling is clamped to the last page");

        frame.setNewValues(indexes.subList(0, 15));
        check(frame.currentScroll == 0 && frame.tiles.size() == 15,
                "A smaller inventory resets its page and displays every slot");
        check(menu.getSlot(0).isActive() && !menu.getSlot(60).isActive(),
                "Changing the inventory clears previously visible slots");
        frame.setNewValues(List.of());
        check(frame.tiles.isEmpty() && !menu.getSlot(0).isActive(), "An empty inventory clears the grid");

        List<Integer> displayOrder = new ArrayList<>(indexes);
        displayOrder.remove(Integer.valueOf(60));
        displayOrder.add(5, 60);
        frame.setNewValues(displayOrder);
        check(frame.tiles.get(5).slot == 60 && menu.getSlot(60).isActive(),
                "The saved final slot is visible in the sixth displayed position");
        check(menu.getSlot(60).x == 268 && menu.getSlot(60).y == 14,
                "The Paxel position is in the first row immediately after Hoe");
        check(menu.getSlot(60).getItem().is(Items.DIAMOND) && menu.getSlot(5).x == 286,
                "Display reordering retains the real slot contents and moves optional slots after Paxel");
        frame.mouseScrolled(200, 30, -1);
        frame.update(200, 30);
        check(!menu.getSlot(60).isActive() && menu.getSlot(59).isActive(),
                "Scrolling the reordered grid hides the first row and reaches the last optional slot");
        frame.mouseScrolled(200, 30, 1);
        frame.update(200, 30);
        check(menu.getSlot(60).isActive() && menu.getSlot(60).x == 268 && menu.getSlot(60).y == 14,
                "Returning to the first page restores the Paxel position");

        TestMenu enlargedMenu = new TestMenu(71);
        List<Integer> enlargedIndexes = IntStream.range(0, 71).boxed().toList();
        InventoryFrame enlarged = new InventoryFrame(new Rect(178, 14, 353, 118), enlargedMenu, 9, enlargedIndexes, null) {};
        enlarged.mouseScrolled(200, 30, -100);
        enlarged.update(200, 30);
        check(enlarged.currentScroll == 2 && enlarged.tiles.size() == 53,
                "The approved 71-slot Fist reaches its eighth row through the normal scroll clamp");
        check(enlarged.tiles.getFirst().slot == 18 && enlarged.tiles.getLast().slot == 70,
                "Every added general slot and the appended Paxel slot can be reached");
        check(enlargedMenu.getSlot(70).isActive() && enlargedMenu.getSlot(70).getItem().is(Items.DIAMOND),
                "The new final slot is visible with its actual contents");
        check(enlargedMenu.getSlot(70).x == 304 && enlargedMenu.getSlot(70).y == 104,
                "The enlarged final row stays within the existing viewport");
        List<Integer> enlargedOrder = new ArrayList<>(enlargedIndexes);
        enlargedOrder.remove(Integer.valueOf(70));
        enlargedOrder.add(5, 70);
        enlarged.currentScroll = 0;
        enlarged.setNewValues(enlargedOrder);
        check(enlarged.tiles.get(5).slot == 70 && enlargedMenu.getSlot(70).x == 268,
                "The new Paxel saved index displays immediately after Hoe");
        enlarged.mouseScrolled(200, 30, -100);
        enlarged.update(200, 30);
        check(enlarged.tiles.getLast().slot == 69 && enlargedMenu.getSlot(69).isActive(),
                "Reordering the enlarged grid keeps the final general slot reachable");

        TestMenu latestMenu = new TestMenu(73);
        List<Integer> latestOrder = new ArrayList<>(IntStream.range(0, 73).boxed().toList());
        latestOrder.remove(Integer.valueOf(71)); latestOrder.add(5, 71);
        latestOrder.remove(Integer.valueOf(72)); latestOrder.add(6, 72);
        InventoryFrame latest = new InventoryFrame(new Rect(178, 14, 353, 118), latestMenu, 9, latestOrder, null) {};
        check(latest.tiles.get(5).slot == 71 && latest.tiles.get(6).slot == 72,
                "Both integration slots occupy their approved first-row positions");
        latest.mouseScrolled(200, 30, -100); latest.update(200, 30);
        check(latest.currentScroll == 3 && latest.tiles.getLast().slot == 70 && latestMenu.getSlot(70).isActive(),
                "The complete 73-slot Fist reaches the ninth row without dropping general slots");
        check(!latestMenu.getSlot(71).isActive() && !latestMenu.getSlot(72).isActive(),
                "Scrolling disables both first-row integration slots");
        latest.mouseScrolled(200, 30, 100); latest.update(200, 30);
        check(latestMenu.getSlot(71).isActive() && latestMenu.getSlot(72).isActive(),
                "Scrolling back restores both dedicated slots");

        InventoryFrame shifted = new InventoryFrame(new Rect(178, 14, 353, 118), latestMenu, 9, latestOrder,
                () -> new MusePoint2D(300, 200)) {};
        check(shifted.mouseScrolled(640, 230, -100) && shifted.currentScroll == 3,
                "Inventory paging uses the rendered container position at the right edge");
        check(!shifted.mouseScrolled(200, 30, 100) && shifted.currentScroll == 3,
                "The unshifted inventory coordinates cannot scroll a displaced container");

        var nativeSlot = new mpsremodularized.common.container.slot.HideableSlotItemHandler(
                new net.neoforged.neoforge.items.ItemStackHandler(1), 0, 0, 0, 0);
        check(!nativeSlot.isActive(), "A real native module slot starts hidden");
        nativeSlot.enable();
        check(nativeSlot.isActive(), "Enabling a real native module slot activates it");
        nativeSlot.disable();
        check(!nativeSlot.isActive(), "Disabling a real native module slot removes it from hit testing");

        ScrollableFrame empty = new ScrollableFrame(new Rect(0, 0, 100, 100));
        VanillaFrameScrollBar bar = new VanillaFrameScrollBar(empty, "regression");
        bar.setMaxValue(0); bar.setValue(0);
        check(Double.isFinite(bar.getValue()) && bar.getValue() == 0,
                "A zero-range scrollbar cannot retain NaN before a larger module list loads");
        empty.setTotalSize(400); bar.setMaxValue(empty.getMaxScrollPixels()); bar.setValue(300);
        check(bar.getValue() == 300 && bar.getKnobSize() > 0,
                "A previously empty scrollbar can reach the final category after loading a larger catalog");
        System.out.println("Installation inventory regression checks passed: " + checks);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static final class TestMenu extends AbstractContainerMenu {
        TestMenu(int size) {
            super(null, 0);
            SimpleContainer inventory = new SimpleContainer(size);
            inventory.setItem(size - 1, new ItemStack(Items.DIAMOND));
            for (int i = 0; i < size; i++) addSlot(new mpsremodularized.common.container.slot.HideableSlot(inventory, 0, i, 0, 0));
        }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
    }

}
