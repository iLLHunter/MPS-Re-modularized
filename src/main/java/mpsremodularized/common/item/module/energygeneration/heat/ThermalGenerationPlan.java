package mpsremodularized.common.item.module.energygeneration.heat;

public record ThermalGenerationPlan(int energy, double heat, double water) {
    public static ThermalGenerationPlan create(double currentHeat, double maxHeat, double baseEnergy, double water) {
        if (!Double.isFinite(currentHeat) || !Double.isFinite(maxHeat) || !Double.isFinite(baseEnergy)
            || currentHeat <= 0 || maxHeat <= 0 || baseEnergy <= 0) {
            return new ThermalGenerationPlan(0, 0, 0);
        }
        double efficiency = Math.min(1, currentHeat / maxHeat);
        int energy = (int) Math.min(Integer.MAX_VALUE, baseEnergy * efficiency);
        return new ThermalGenerationPlan(energy, Math.min(currentHeat, baseEnergy), Math.max(0, water));
    }

    public double heatUsed(int acceptedEnergy) {
        return heat * acceptedFraction(acceptedEnergy);
    }

    public int waterUsed(int acceptedEnergy) {
        return (int) Math.ceil(water * acceptedFraction(acceptedEnergy));
    }

    private double acceptedFraction(int acceptedEnergy) {
        return energy > 0 ? Math.clamp((double) acceptedEnergy / energy, 0, 1) : 0;
    }
}
