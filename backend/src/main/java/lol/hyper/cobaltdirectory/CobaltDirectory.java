package lol.hyper.cobaltdirectory;

import lol.hyper.cobaltdirectory.instance.Instance;
import lol.hyper.cobaltdirectory.requests.ApiCheck;
import lol.hyper.cobaltdirectory.services.Services;
import lol.hyper.cobaltdirectory.tests.Test;
import lol.hyper.cobaltdirectory.tests.TestBuilder;
import lol.hyper.cobaltdirectory.tests.TestResult;
import lol.hyper.cobaltdirectory.utils.FileUtil;
import lol.hyper.cobaltdirectory.utils.ProxyInfo;
import lol.hyper.cobaltdirectory.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CobaltDirectory {

    private static Logger logger;
    private static String USER_AGENT;
    private static final ReusableMessageFactory MESSAGE_FACTORY = new ReusableMessageFactory();
    private static ProxyInfo proxyInfo = null;

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

        // check if we should use a proxy
        if (init.useProxy()) {
            proxyInfo = new ProxyInfo(init.getProxyHost(), init.getProxyPort());
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
        int online = 0;
        int offline = 0;
        for (Instance instance : instances) {
            // only create tests if the API is working
            if (instance.isApiWorking()) {
                online++;
                String token = null;
                String api = instance.getApi();
                logger.info("{} is ONLINE", instance.getApi());
                boolean apiKey = init.getApiKeys().has(api);
                // if turnstile is enabled, and we have no API key
                // skip tests since we can't do anything
                boolean skipTests = instance.hasTurnstile() && !apiKey;
                if (skipTests) {
                    logger.warn("Skipping ALL tests for {} since it has Cloudflare turnstile and we have no API key", api);
                }
                // if we have an API key for this instance, use it for tests
                if (apiKey) {
                    logger.info("Found API key for {}, will use it for requests", api);
                    token = init.getApiKeys().getString(api);
                }
                // create the tests for each service for this instance
                for (Map.Entry<String, String> tests : services.getTests().entrySet()) {
                    String service = tests.getKey();
                    String url = tests.getValue();
                    // skip the tests
                    if (skipTests) {
                        TestResult skippedTest = new TestResult(service, false, "Uses Cloudflare turnstile, unable to test via API (no API key)");
                        instance.addResult(skippedTest);
                    } else {
                        Test test = new Test(instance, service, url, token);
                        testsToRun.add(test);
                    }
                }
                // if the frontend is not null, add it to the tests
                if (instance.getFrontEnd() != null) {
                    Test frontEndTest = new Test(instance, "Frontend", instance.getProtocol() + "://" + instance.getFrontEnd(), null);
                    testsToRun.add(frontEndTest);
                }
            } else {
                logger.warn("{} is OFFLINE", instance.getApi());
                offline++;
            }
        }

        double onlinePercent = (double) Math.round((double) online / (online + offline) * 100) / 100;
        double offlinePercent = (double) Math.round((double) offline / (online + offline) * 100) / 100;
        logger.info("Online: {} - {}%", online, onlinePercent);
        logger.info("Offline: {} - {}%", offline, offlinePercent);

        if (testsToRun.isEmpty()) {
            logger.warn("No tests to run, exiting...");
            System.exit(0);
        }

        // perform the service tests
        Collections.shuffle(testsToRun);
        testBuilder.runServiceTests(testsToRun);

        // set when the tests ran. this will be afterward, as it's more for "as of this time"
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = f.format(new Date());

        // check for dupes
        findDuplicates(instances);

        JSONArray testResults = new JSONArray();
        File testResultsOutput = new File("results.json");
        Map<String, TestCounter> testResultsCounter = new HashMap<>();
        // idk what to call this
        Map<String, List<Instance>> servicesWithWorkingInstances = new HashMap<>();
        for (String service : services.getServices()) {
            servicesWithWorkingInstances.put(service, new ArrayList<>());
        }

        for (Instance instance : instances) {

            JSONObject instanceResults = new JSONObject();
            instanceResults.put("api", instance.getApi());
            instanceResults.put("frontend", instance.getFrontEnd());
            instanceResults.put("protocol", instance.getProtocol());
            instanceResults.put("online", instance.isApiWorking());
            instanceResults.put("version", instance.getVersion());
            instanceResults.put("remote", instance.getRemote());

            JSONObject tests = new JSONObject();

            for (TestResult r : instance.getTestResults()) {
                JSONObject serviceResults = new JSONObject();
                String service = r.service();
                boolean status = r.status();

                serviceResults.put("status", status);
                serviceResults.put("message", StringUtil.makeLogPretty(r.message()));
                serviceResults.put("friendly", Services.getIdToFriendly().get(r.service()));

                TestCounter c = testResultsCounter.computeIfAbsent(r.service(), s -> new TestCounter());
                c.total++;

                if (status) {
                    if (!r.service().equals("Frontend")) {
                        servicesWithWorkingInstances.computeIfAbsent(service, k -> new ArrayList<>()).add(instance);
                    }
                    c.success++;
                }

                tests.put(service, serviceResults);
            }

            instanceResults.put("tests", tests);
            testResults.put(instanceResults);
        }

        // store which service support what instance
        JSONObject serviceApi = new JSONObject();
        JSONObject serviceFrontendsApi = new JSONObject();
        for (Map.Entry<String, List<Instance>> entry : servicesWithWorkingInstances.entrySet()) {
            String service = entry.getKey();
            String friendlyService = Services.getIdToFriendly().get(service);
            List<Instance> workingInstances = entry.getValue();

            JSONArray workingApiArray = new JSONArray();
            JSONArray workingFrontendArray = new JSONArray();
            for (Instance instance : workingInstances) {
                workingApiArray.put(instance.getProtocol() + "://" + instance.getApi());
                if (instance.getFrontEnd() != null) {
                    workingFrontendArray.put(instance.getProtocol() + "://" + instance.getFrontEnd());
                }
            }

            if (workingInstances.isEmpty()) {
                logger.warn("No working instances for {}", friendlyService);
            }

            serviceApi.put(service, workingApiArray);
            serviceFrontendsApi.put(service, workingFrontendArray);
        }

        File serviceApiFile = new File("api.json");
        File serviceFrontendsApiFile = new File("api_frontends.json");
        FileUtil.writeFile(serviceApi.toString(), serviceApiFile);
        FileUtil.writeFile(serviceFrontendsApi.toString(), serviceFrontendsApiFile);

        // get the longest running instance for fun
        Instance oldestInstance = instances.stream()
                .filter(Instance::isApiWorking).filter(instance -> instance.getStartTime() != 0)
                .min(Comparator.comparingLong(Instance::getStartTime))
                .orElseThrow(() -> new IllegalStateException("No instance with a valid startTime"));

        testResultsCounter.forEach((service, c) -> logger.info("{}: {}/{}", Services.getIdToFriendly().get(service), c.success, c.total));

        logger.info("Oldest instance is: {}, starTime={}", oldestInstance.getApi(), oldestInstance.getStartTime());

        logger.info("Saving results to {}", testResultsOutput.getAbsolutePath());
        FileUtil.writeFile(testResults.toString(), testResultsOutput);

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
                    logger.warn("- {}", inst.getApi());
                }
            }
        }
    }

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public static ReusableMessageFactory getMessageFactory() {
        return MESSAGE_FACTORY;
    }

    public static boolean useProxy() {
        return proxyInfo != null;
    }

    public static ProxyInfo getProxyInfo() {
        return proxyInfo;
    }
}