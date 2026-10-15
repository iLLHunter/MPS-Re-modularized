package mpsremodularized.common.item.module.energygeneration.heat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnergyModuleRegressionTest {
    private static int checks;

    public static void main(String[] args) throws IOException {
        ThermalGenerationPlan plan = ThermalGenerationPlan.create(100, 200, 3000, 3000);
        check(plan.energy() == 1500, "Efficiency scales generation once");
        check(plan.heatUsed(1500) == 100, "Full acceptance removes planned heat");
        check(plan.waterUsed(1500) == 3000, "Full acceptance consumes planned water");
        check(plan.heatUsed(0) == 0 && plan.waterUsed(0) == 0, "Full batteries spend no resources");
        check(plan.heatUsed(750) == 50 && plan.waterUsed(750) == 1500, "Partial acceptance scales both resources");
        check(plan.heatUsed(3000) == 100 && plan.waterUsed(3000) == 3000, "Resource use cannot exceed the plan");
        check(plan.heatUsed(-1) == 0 && plan.waterUsed(-1) == 0, "Negative acceptance spends no resources");
        check(ThermalGenerationPlan.create(0, 200, 3000, 3000).energy() == 0, "Cold equipment generates nothing");
        check(ThermalGenerationPlan.create(100, 0, 3000, 3000).energy() == 0, "Missing heat capacity is safe");
        check(ThermalGenerationPlan.create(100, 200, 0, 3000).waterUsed(1) == 0, "Disabled generation consumes no water");
        check(ThermalGenerationPlan.create(0.001, 200, 1, 3000).waterUsed(1) == 0, "Sub-FE output consumes no water");
        check(ThermalGenerationPlan.create(300, 200, 3000, 3000).energy() == 3000, "Efficiency is capped at one");
        check(ThermalGenerationPlan.create(Double.NaN, 200, 3000, 3000).energy() == 0, "Invalid heat cannot generate energy");
        ThermalGenerationPlan lowWater = ThermalGenerationPlan.create(100, 200, 500, 500);
        check(lowWater.energy() == 250 && lowWater.waterUsed(250) == 500, "Limited water produces a proportional pulse");

        String generators = source("config/module/EnergyGenerationModuleConfig.java");
        String tanks = source("config/module/FluidStorageConfig.java");
        String fist = source("config/PowerFistConfig.java");
        for (int tier = 1; tier <= 4; tier++) {
            int multiplier = 1 << (tier - 1);
            check(defaultValue(generators, "SOLAR_ENERGY_GENERATOR_MODULE_" + tier + "__ENERGY_GENERATION_DAY_BASE") == 15000 * multiplier, "Solar day tier " + tier);
            check(defaultValue(generators, "SOLAR_ENERGY_GENERATOR_MODULE_" + tier + "__ENERGY_GENERATION_NIGHT_BASE") == 1500 * multiplier, "Solar night tier " + tier);
            check(defaultValue(generators, "THERMAL_ENERGY_GENERATOR_MODULE_" + tier + "__THERMOELECTRIC_ENERGY_GENERATION") == 250 * multiplier, "Thermal dry tier " + tier);
            check(defaultValue(generators, "THERMAL_ENERGY_GENERATOR_MODULE_" + tier + "__STEAM_ELECTRIC_ENERGY_GENERATION_BASE") == 150 * multiplier, "Steam base tier " + tier);
            check(defaultValue(generators, "THERMAL_ENERGY_GENERATOR_MODULE_" + tier + "__STEAM_ELECTRIC_ENERGY_GENERATION_MULTIPLIER") == 500 * multiplier, "Steam slider tier " + tier);
            check(defaultValue(tanks, "COOLANT_TANK_MODULE_" + tier + "_CAPACITY") == 1000 * multiplier, "Tank capacity tier " + tier);
            check(defaultValue(tanks, "COOLANT_TANK_MODULE_" + tier + "__ENERGY_CONSUMPTION_COOLANT_BONUS_MULTIPLIER") == multiplier, "Electric cooling tier " + tier);
            check(defaultValue(fist, "POWER_FIST_" + tier + "__MAX_HEAT") == 50 * multiplier, "Power Fist heat tier " + tier);
        }
        System.out.println("Energy module regression checks passed: " + checks);
    }

    private static String source(String relativePath) throws IOException {
        return Files.readString(Path.of("src/main/java/mpsremodularized/common", relativePath));
    }

    private static double defaultValue(String source, String field) {
        Matcher matcher = Pattern.compile(Pattern.quote(field) + "\\s*=.*?defineInRange\\([^,]+,\\s*([0-9.]+)[Dd]?,").matcher(source);
        if (!matcher.find()) {
            throw new AssertionError("Missing config default: " + field);
        }
        return Double.parseDouble(matcher.group(1));
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
