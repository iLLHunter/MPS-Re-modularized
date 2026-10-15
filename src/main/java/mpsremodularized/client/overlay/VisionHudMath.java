package mpsremodularized.client.overlay;

/** Display arithmetic kept independent of the client so boundary checks can run headlessly. */
public final class VisionHudMath {
    private VisionHudMath() {}

    public static String clock(long dayTime, boolean twentyFourHour) {
        int hour = (int) ((Math.floorMod(dayTime, 24000L) / 1000 + 6) % 24);
        if (twentyFourHour) return Integer.toString(hour);
        int twelveHour = hour % 12;
        return (twelveHour == 0 ? 12 : twelveHour) + (hour < 12 ? " AM" : " PM");
    }

    public static float zoom(float incomingFov, double divisor) {
        if (!Double.isFinite(divisor) || divisor <= 0) return incomingFov;
        float result = (float) (incomingFov / divisor);
        return Float.isFinite(result) && result > 0 ? result : incomingFov;
    }
}
