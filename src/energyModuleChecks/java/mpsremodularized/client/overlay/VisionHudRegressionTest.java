package mpsremodularized.client.overlay;

public final class VisionHudRegressionTest {
    private static int checks;
    public static void main(String[] args) {
        long[] times = {0, 6000, 12000, 18000, 23000, 24000, -6000, -24000, 18000000};
        String[] military = {"6", "12", "18", "0", "5", "6", "0", "6", "6"};
        String[] standard = {"6 AM", "12 PM", "6 PM", "12 AM", "5 AM", "6 AM", "12 AM", "6 AM", "6 AM"};
        for (int i = 0; i < times.length; i++) {
            check(VisionHudMath.clock(times[i], true).equals(military[i]), "24-hour boundary at " + times[i]);
            check(VisionHudMath.clock(times[i], false).equals(standard[i]), "12-hour boundary at " + times[i]);
        }
        check(Math.abs(VisionHudMath.zoom(1.15F, 10) - 0.115F) < 1e-6, "Zoom composes with incoming sprint/normalization FOV");
        check(Math.abs(VisionHudMath.zoom(0.8F, 0.5) - 1.6F) < 1e-6, "Configured wide-angle zoom remains supported");
        for (double divisor : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MIN_VALUE, Double.MAX_VALUE}) {
            check(VisionHudMath.zoom(1.15F, divisor) == 1.15F, "Degenerate zoom preserves a finite incoming FOV: " + divisor);
        }
        System.out.println("Vision/HUD regression checks passed: " + checks);
    }
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
}
