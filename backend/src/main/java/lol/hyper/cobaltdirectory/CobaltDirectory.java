package lol.hyper.cobaltdirectory;

import lol.hyper.cobaltdirectory.instance.Instance;
import lol.hyper.cobaltdirectory.requests.ApiCheck;
import lol.hyper.cobaltdirectory.requests.Test;
import lol.hyper.cobaltdirectory.requests.TestResult;
import lol.hyper.cobaltdirectory.services.Services;
import lol.hyper.cobaltdirectory.tests.TestBuilder;
import lol.hyper.cobaltdirectory.utils.StringUtil;
import lol.hyper.cobaltdirectory.web.WebBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CobaltDirectory {

    public static Logger logger;
    public static String USER_AGENT;
    public static JSONObject config;
    public static final ReusableMessageFactory MESSAGE_FACTORY = new ReusableMessageFactory();

    static class TestCounter {
        int success;
        int total;
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        logger = LogManager.getLogger(CobaltDirectory.class);
        logger.info("Running with args: {}", Arrays.toString(args));

        Init init = new Init();
        init.start(args);

        // load the config
        config = init.getConfig();
        if (config == null) {
            logger.error("Unable to load config! Exiting...");
            System.exit(1);
        }

        // set the user agent
        USER_AGENT = init.getUserAgent();

        // load the tests into services
        Services services = new Services(init.getTests());
        services.importTests();

        // shuffle the lists here
        Collections.shuffle(init.getInstanceFileContents());

        // load the instance file and build each instance
        List<Instance> instances = new ArrayList<>();
        Set<String> duplicates = new HashSet<>();
        for (String line : init.getInstanceFileContents()) {
            // each line is formatted api,frontend,protocol
            // we can split this and get each part
            List<String> lineFix = Arrays.asList(line.split(","));
            String api = lineFix.get(0);
            String frontEnd = lineFix.get(1);
            String protocol = lineFix.get(2);

            if (duplicates.contains(api)) {
                logger.warn("Duplicate api found: {}!!!! Ignoring...", api);
                continue;
            }

            duplicates.add(api);

            // if the instance has "None" set for the frontend
            if (frontEnd.equals("None")) {
                frontEnd = null;
            }
            // build the instance
            logger.info("Setting up instance {}", api);
            Instance newInstance = new Instance(frontEnd, api, protocol);
            newInstance.setHash(StringUtil.makeHash(api));
            instances.add(newInstance);
        }

        // create the tests to make sure each API works
        List<ApiCheck> apiChecks = new ArrayList<>();
        for (Instance instance : instances) {
            ApiCheck apiCheck = new ApiCheck(instance);
            apiChecks.add(apiCheck);
        }

        // create the test builder, which performs the tests
        TestBuilder testBuilder = new TestBuilder();
        // check the APIs to see if they work
        testBuilder.runApiInfoTests(apiChecks);

        // create tests for all APIs that are working
        List<Test> testsToRun = new ArrayList<>();
        for (Instance instance : instances) {
            // only create tests if the API is working
            if (instance.isApiWorking()) {
                String token = null;
                String api = instance.getApi();
                logger.info("{} is ONLINE", instance.getApi());
                // if we have an API key for this instance, use it for tests
                if (init.getApiKeys().has(api)) {
                    logger.info("Found API key for {}, will use it for requests", api);
                    token = init.getApiKeys().getString(api);
                }
                // create the tests for each service for this instance
                for (Map.Entry<String, String> tests : services.getTests().entrySet()) {
                    String service = tests.getKey();
                    String url = tests.getValue();
                    Test test = new Test(instance, service, url, token);
                    testsToRun.add(test);
                }
                // if the frontend is not null, add it to the tests
                if (instance.getFrontEnd() != null) {
                    Test frontEndTest = new Test(instance, "Frontend", instance.getProtocol() + "://" + instance.getFrontEnd(), null);
                    testsToRun.add(frontEndTest);
                }
            } else {
                logger.info("{} is OFFLINE", instance.getApi());
            }
        }

        // perform the service tests
        Collections.shuffle(testsToRun);
        testBuilder.runServiceTests(testsToRun);

        // set when the tests ran. this will be afterward, as it's more for "as of this time"
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = f.format(new Date());

        // calculate the scores for all instances
        instances.forEach(Instance::calculateScore);

        // check for dupes
        findDuplicates(instances);

        Map<String, TestCounter> testResults = new HashMap<>();
        for (Instance instance : instances) {
            if (!instance.isApiWorking()) {
                instance.setScore(-1.0);
                continue;
            }

            for (TestResult r : instance.getTestResults()) {
                TestCounter c = testResults.computeIfAbsent(r.service(), s -> new TestCounter());
                c.total++;
                if (r.status()) c.success++;
            }

            // write each instance page
            WebBuilder.buildInstancePage(instance, formattedDate);
        }

        // sort the instances by score
        instances.sort(Comparator.comparingDouble(Instance::getScore).reversed());

        // get the longest running instance for fun
        Instance oldestInstance = instances.stream()
                .filter(Instance::isApiWorking)
                .min(Comparator.comparingLong(Instance::getStartTime))
                .orElseThrow(() -> new IllegalStateException("No instance with a valid startTime"));

        testResults.forEach((service, c) -> logger.info("{}: {}/{}", service, c.success, c.total));

        logger.info("Oldest instance is: {}, starTime={}", oldestInstance.getApi(), oldestInstance.getStartTime());

        // write index and service pages
        WebBuilder.buildIndex(instances, formattedDate);
        for (String service : services.getTests().keySet()) {
            String slug = Services.makeSlug(service);
            WebBuilder.buildServicePage(instances, formattedDate, service, slug);
        }

        // display how long the test took
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        long minutesTaken = TimeUnit.MINUTES.convert(duration, TimeUnit.NANOSECONDS);
        logger.info("Completed run in {} minutes.", minutesTaken);
        System.exit(0);
    }

    public static void findDuplicates(List<Instance> instances) {
        Map<Long, List<Instance>> map = new HashMap<>();

        for (Instance instance : instances) {
            if (instance.getStartTime() == 0) {
                continue;
            }
            map.computeIfAbsent(instance.getStartTime(), k -> new ArrayList<>()).add(instance);
        }

        for (Map.Entry<Long, List<Instance>> entry : map.entrySet()) {
            List<Instance> duplicates = entry.getValue();
            if (duplicates.size() > 1) {
                logger.warn("!!! Duplicate start time {} found !!!", entry.getKey());
                for (Instance inst : duplicates) {
                    logger.warn("- {}", inst.getName());
                }
            }
        }
    }
}