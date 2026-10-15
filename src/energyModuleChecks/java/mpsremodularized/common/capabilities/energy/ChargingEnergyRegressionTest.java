package mpsremodularized.common.capabilities.energy;

import mpsremodularized.common.utils.ElectricItemUtils;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class ChargingEnergyRegressionTest {
    private static int checks;

    public static void main(String[] args) {
        for (int sourceLimit : new int[]{0, 1, 10_000, 1_250_000}) {
            for (int targetLimit : new int[]{0, 1, 7, 1_250_000}) {
                EnergyStorage source = new EnergyStorage(1_000_000, sourceLimit, sourceLimit, 1_000_000);
                EnergyStorage target = new EnergyStorage(1_000_000, targetLimit, targetLimit);
                int charged = ElectricItemUtils.transferEnergy(source, target, 10_000);
                check(charged == Math.min(10_000, Math.min(sourceLimit, targetLimit)), "Both transfer limits constrain a pulse");
                check(charged == target.getEnergyStored(), "Receiver credits equal the reported transfer");
                check(source.getEnergyStored() + target.getEnergyStored() == 1_000_000, "A charging pulse conserves energy");
            }
        }
        EnergyStorage source = new EnergyStorage(1000, 1000, 1000, 1000);
        EnergyStorage almostFull = new EnergyStorage(1000, 1000, 1000, 997);
        check(ElectricItemUtils.transferEnergy(source, almostFull, 1000) == 3, "Partial room withdraws only accepted energy");
        check(source.getEnergyStored() == 997 && almostFull.getEnergyStored() == 1000, "Partial transfer has matching debit and credit");
        check(ElectricItemUtils.transferEnergy(source, almostFull, 1000) == 0 && source.getEnergyStored() == 997,
                "Full receivers do not drain the source");
        check(ElectricItemUtils.transferEnergy(source, new EnergyStorage(1000), 0) == 0, "A zero budget is safe");

        EnergyStorage tile = new EnergyStorage(1_000_000, 10_000, 10_000, 10_000);
        EnergyStorage battery = new EnergyStorage(100_000, 10_000, 10_000, 40_000);
        TestWrapper wrapper = new TestWrapper(tile, battery);
        check(wrapper.getEnergyStored() == 50_000, "Inserted battery counted once");
        check(wrapper.getMaxEnergyStored() == 1_100_000, "Inserted battery capacity counted once");
        check(wrapper.extractEnergy(15_000, true) == 15_000 && !wrapper.isDirty(), "Simulated withdrawals do not dirty storage");
        check(wrapper.getEnergyStored() == 50_000, "Simulation leaves both stores intact");
        check(wrapper.extractEnergy(15_000, false) == 15_000 && wrapper.isDirty(), "Actual withdrawals mark changed storage");
        check(tile.getEnergyStored() == 0 && battery.getEnergyStored() == 35_000, "Block buffer is drained before its battery");
        wrapper.setDirty(false);
        check(wrapper.receiveEnergy(15_000, true) == 15_000 && !wrapper.isDirty(), "Simulated receipts do not dirty storage");
        check(wrapper.receiveEnergy(15_000, false) == 15_000 && wrapper.isDirty(), "Actual receipts mark changed storage");

        TestWrapper noBattery = new TestWrapper(new EnergyStorage(1_000_000, 10_000), null);
        check(noBattery.receiveEnergy(10_000, false) == 10_000 && noBattery.isDirty(), "Internal-only charging is a saved change");
        noBattery.setDirty(false);
        check(noBattery.extractEnergy(10_000, false) == 10_000 && noBattery.isDirty(), "Internal-only extraction is a saved change");
        noBattery.setDirty(false);
        check(noBattery.extractEnergy(1, false) == 0 && !noBattery.isDirty(), "No-op transfers do not mark a change");
        TestWrapper large = new TestWrapper(new EnergyStorage(10_000_000), new EnergyStorage(Integer.MAX_VALUE));
        check(large.getMaxEnergyStored() == Integer.MAX_VALUE, "Valid large capacities cannot overflow the energy API's integer");
        System.out.println("Charging energy regression checks passed: " + checks);
    }

    private static final class TestWrapper extends BlockEnergyWrapper {
        private final IEnergyStorage battery;
        TestWrapper(IEnergyStorage tile, IEnergyStorage battery) {
            super(tile, null);
            this.battery = battery;
        }
        @Override IEnergyStorage getItemEnergyHandler() { return battery; }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
