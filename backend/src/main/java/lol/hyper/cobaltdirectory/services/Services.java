package lol.hyper.cobaltdirectory.services;

import lol.hyper.cobaltdirectory.CobaltDirectory;
import lol.hyper.cobaltdirectory.utils.XiaohongshuTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Services {

    private final JSONObject tests;
    private final Map<String, String> testsUrls = new HashMap<>();
    private final Logger logger = LogManager.getLogger(Services.class, CobaltDirectory.MESSAGE_FACTORY);

    public Services(JSONObject tests) {
        this.tests = tests;
    }

    public void importTests() {
        for (String key : tests.keySet()) {
            logger.info("Importing test: {}", key);
            if (key.equalsIgnoreCase("xiaohongshu")) {
                logger.info("Generating Xiaohongshu test video...");
                String xiaohongshuUrl = XiaohongshuTest.getTestUrl();
                if (xiaohongshuUrl == null) {
                    logger.warn("Unable to dynamically get Xiaohongshu link!");
                } else {
                    logger.info("Found valid Xiaohongshu link: {}", xiaohongshuUrl);
                    testsUrls.put(key, xiaohongshuUrl);
                    continue;
                }
            }
            testsUrls.put(key, tests.getString(key));
        }
    }

    public static String makeSlug(String service) {
        String slug = service.toLowerCase(Locale.ROOT);
        return slug.replace(" ", "-");
    }

    public Map<String, String> getTests() {
        return testsUrls;
    }
}
