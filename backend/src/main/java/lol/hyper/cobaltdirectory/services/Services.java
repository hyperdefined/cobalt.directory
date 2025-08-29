package lol.hyper.cobaltdirectory.services;

import lol.hyper.cobaltdirectory.CobaltDirectory;
import lol.hyper.cobaltdirectory.tests.XiaohongshuTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.*;

public class Services {

    private final JSONObject tests;
    private final Map<String, String> testsUrls = new HashMap<>();
    private static final Map<String, String> idToFriendly = new HashMap<>();
    private final Logger logger = LogManager.getLogger(Services.class, CobaltDirectory.getMessageFactory());
    private final List<String> services = new ArrayList<>();

    public Services(JSONObject tests) {
        this.tests = tests;
    }

    public void importTests() {
        for (String service : tests.keySet()) {
            String serviceId = makeSlug(service);

            logger.info("Importing test: {}", service);
            if (service.equalsIgnoreCase("xiaohongshu")) {
                logger.info("Generating Xiaohongshu test video...");
                String xiaohongshuUrl = XiaohongshuTest.getTestUrl();
                if (xiaohongshuUrl == null) {
                    logger.warn("Unable to dynamically get Xiaohongshu link!");
                } else {
                    logger.info("Found valid Xiaohongshu link: {}", xiaohongshuUrl);
                    testsUrls.put(serviceId, xiaohongshuUrl);
                    continue;
                }
            }
            services.add(serviceId);
            idToFriendly.put(serviceId, service);
            testsUrls.put(serviceId, tests.getString(service));
        }
    }

    public static String makeSlug(String service) {
        String slug = service.toLowerCase(Locale.ROOT);
        return slug.replace(" ", "-");
    }

    public Map<String, String> getTests() {
        return testsUrls;
    }

    public List<String> getServices() {
        return services;
    }

    public static Map<String, String> getIdToFriendly() {
        return idToFriendly;
    }
}
