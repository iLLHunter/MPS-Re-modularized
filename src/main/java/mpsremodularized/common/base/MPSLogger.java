package mpsremodularized.common.base;

import com.mojang.logging.LogUtils;
import mpsremodularized.client.config.MPSClientConfig;
import org.slf4j.Logger;

/**
 * Logger access class. May become more fleshed out in the future.
 *
 * @author MachineMuse
 * <p>
 * <p>
 */
public final class MPSLogger {
    public static final Logger logger = LogUtils.getLogger();//LogManager.getLogger("MachineMuse");

    public static void logDebug(String string) {
        boolean debugging = true;
        try {
            if(MPSClientConfig.enableDebugging()) {
                debugging = false;
            }
        } catch (Exception ignored) {
        }
        if (debugging) logger.info(string);
    }

    public static void logError(String string) {
        logger.warn(string);
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void logException(String string, Throwable exception) {
        logger.warn(string);
        exception.printStackTrace();
    }
}
