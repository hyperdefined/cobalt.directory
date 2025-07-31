package lol.hyper.cobaltdirectory.tests;

import lol.hyper.cobaltdirectory.requests.ApiCheck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.*;

public class TestBuilder {

    private ExecutorService executorService;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final Logger logger = LogManager.getLogger(this);

    public void runApiInfoTests(List<ApiCheck> tests) {
        ConcurrentLinkedQueue<ApiCheck> apiQueue = new ConcurrentLinkedQueue<>(tests);
        CountDownLatch latch = new CountDownLatch(apiQueue.size());
        int totalApis = apiQueue.size();
        logger.info("Total APIs to check: {}", totalApis);

        executorService = Executors.newFixedThreadPool(CORES * 2);

        // queue all tests
        while (!apiQueue.isEmpty()) {
            ApiCheck apiCheck = apiQueue.poll();
            if (apiCheck != null) {
                executorService.submit(() -> {
                    try {
                        apiCheck.run();
                    } catch (Exception exception) {
                        logger.error("API check failed due to an exception: {}", apiCheck, exception);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        executorService.shutdown();

        try {
            while (!latch.await(20, TimeUnit.SECONDS)) {
                logger.info("Remaining API checks: {}", latch.getCount());
                if (!apiQueue.isEmpty()) {
                    logger.info("APIs still in queue: {}", apiQueue);
                }
            }
        } catch (InterruptedException exception) {
            logger.error("Execution was interrupted", exception);
        }

        // check if tests completed
        if (latch.getCount() == 0) {
            logger.info("All API checks have completed!!!!");
        } else {
            logger.error("There are API checks remaining that we did not complete :(((");
            logger.error("API checks left: {}", latch.getCount());
        }
    }

    public void runServiceTests(List<Test> tests) {
        ConcurrentLinkedQueue<Test> testsQueue = new ConcurrentLinkedQueue<>(tests);
        CountDownLatch latch = new CountDownLatch(testsQueue.size());
        int totalTests = testsQueue.size();
        logger.info("Total tests to process: {}", totalTests);

        executorService = Executors.newFixedThreadPool(CORES * 2);

        // queue all tests
        while (!testsQueue.isEmpty()) {
            Test test = testsQueue.poll();
            if (test != null) {
                executorService.submit(() -> {
                    try {
                        test.run();
                    } catch (Exception exception) {
                        logger.error("Test failed due to an exception: {}", test, exception);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        executorService.shutdown();

        try {
            while (!latch.await(20, TimeUnit.SECONDS)) {
                logger.info("Remaining tests: {}", latch.getCount());
                if (!testsQueue.isEmpty()) {
                    logger.info("Tests still in queue: {}", testsQueue);
                }
            }
        } catch (InterruptedException exception) {
            logger.error("Execution was interrupted", exception);
        }

        // check if tests completed
        if (latch.getCount() == 0) {
            logger.info("All tests have completed!!!!");
        } else {
            logger.error("There are tests remaining that we did not complete :(((");
            logger.error("Tests left: {}", latch.getCount());
        }
    }
}
