package lol.hyper.cobaltdirectory.tests;

import lol.hyper.cobaltdirectory.CobaltDirectory;
import lol.hyper.cobaltdirectory.instance.Instance;
import lol.hyper.cobaltdirectory.requests.RequestResults;
import lol.hyper.cobaltdirectory.utils.RequestUtil;
import lol.hyper.cobaltdirectory.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Test {

    private final Instance instance;
    private final String service;
    private final String testUrl;
    private final String authorization;
    private final Logger logger = LogManager.getLogger(Test.class, CobaltDirectory.getMessageFactory());
    private int attempts = 0;
    private final List<String> validStatus = Arrays.asList("redirect", "stream", "tunnel", "success", "picker", "local-processing");
    private String api;

    public Test(Instance instance, String service, String testUrl, String authorization) {
        this.instance = instance;
        this.service = service;
        this.testUrl = testUrl;
        this.authorization = authorization;
    }

    public void run() {
        if (service.equalsIgnoreCase("Frontend")) {
            runFrontEndTest();
        } else {
            runApiTest();
        }
    }

    private void runFrontEndTest() {
        boolean validFrontEnd = RequestUtil.testFrontEnd(testUrl);
        if (validFrontEnd) {
            instance.addResult(new TestResult(service, true, "Working"));
            logger.info("Test PASS for checking frontend {} ", testUrl);
        } else {
            logger.info("Test FAIL for checking frontend {} ", testUrl);
            instance.addResult(new TestResult(service, false, null));
        }
        instance.setFrontEndWorking(validFrontEnd);
    }

    private void runApiTest() {
        long start = System.nanoTime();
        String protocol = instance.getProtocol();
        if (instance.is10()) {
            api = protocol + "://" + instance.getApi();
        } else {
            api = protocol + "://" + instance.getApi() + "/api/json";
        }
        JSONObject postContents = new JSONObject();
        postContents.put("url", testUrl);
        RequestResults testResponse = RequestUtil.sendPost(postContents, api, authorization);
        String content = testResponse.responseContent();
        int responseCode = testResponse.responseCode();
        Exception exception = testResponse.exception();
        long time = TimeUnit.MILLISECONDS.convert((System.nanoTime() - start), TimeUnit.NANOSECONDS);
        // check if there are exceptions first
        if (exception != null) {
            logger.warn("Test FAIL for {} with {} - HTTP {}, reason={}, time={}ms", api, service, responseCode, exception.toString(), time);
            instance.addResult(new TestResult(service, false, exception.toString()));
            return;
        }
        // check if the content returned was null
        if (content == null) {
            logger.warn("Test FAIL for {} with {} - HTTP {}, time={}ms response content returned null", api, service, responseCode, time);
            instance.addResult(new TestResult(service, false, "Response content returned null from API"));
            return;
        }
        // make sure we can parse the response from the API
        JSONObject jsonResponse;
        try {
            jsonResponse = new JSONObject(content);
        } catch (JSONException jsonException) {
            logger.warn("Test FAIL for {} with {} - HTTP {}, reason={}, time={}ms", api, service, responseCode, jsonException.toString(), time);
            instance.addResult(new TestResult(service, false, jsonException.toString()));
            return;
        }
        // get the status of the API request
        // just in case it randomly doesn't return
        String status;
        if (jsonResponse.has("status")) {
            status = jsonResponse.getString("status");
        } else {
            status = "UNKNOWN";
        }
        // count the attempts here, since the previous ones failed
        attempts++;
        // if the API's response was HTTP 200, it most likely worked
        if (responseCode == 200) {
            // if the response has a status key, then read it. otherwise, it probably failed
            if (status == null) {
                logger.warn("Test FAIL for {} with {} - HTTP 200, status=INVALID, time={}ms", api, service, time);
                instance.addResult(new TestResult(service, false, "Status returned null, HTTP " + responseCode));
                return;
            }

            // check if the status is valid in the response JSON
            if (validStatus.contains(status.toLowerCase(Locale.ROOT))) {
                // if the response was tunnel or stream, check the headers
                // cobalt has content-length and estimated-content-length headers
                // they report how big the media is
                if (status.equalsIgnoreCase("tunnel") || status.equalsIgnoreCase("stream")) {
                    // make sure the tunnel link returns the correct domain
                    // some APIs never do this
                    String tunnelUrl = StringUtil.rewrite(jsonResponse.getString("url"), instance.getApi(), protocol);
                    checkHeaders(tunnelUrl, status, time);
                    return;
                }
                if (status.equalsIgnoreCase("local-processing")) {
                    // make sure we have tunnel links
                    // not sure if this is ever possible to fail
                    if (!jsonResponse.has("tunnel")) {
                        logger.error("Test FAIL for {} with {} - HTTP 200, status={}, time={}ms - local-processing but no tunnel links", api, service, status, time);
                        instance.addResult(new TestResult(service, false, "Forced local-processing, but no tunnel links returned"));
                        return;
                    }
                    // check the headers for the tunnel urls
                    // it returns multiple, so just check the first one
                    JSONArray tunnelUrls = jsonResponse.getJSONArray("tunnel");
                    String tunnelUrl = StringUtil.rewrite(tunnelUrls.getString(0), instance.getApi(), protocol);
                    checkHeaders(tunnelUrl, status, time);
                    return;
                }
                if (status.equalsIgnoreCase("picker")) {
                    JSONArray photos = jsonResponse.getJSONArray("picker");
                    logger.info("Test PASS for {} with {} - HTTP 200, status={}, time={}ms, photos={}", api, service, status, time, photos.length());
                    instance.addResult(new TestResult(service, true, "Working, returned valid status (" + status + " with " + photos.length() + " photos)"));
                    return;
                }
                logger.info("Test PASS for {} with {} - HTTP 200, status={}, time={}ms", api, service, status, time);
                instance.addResult(new TestResult(service, true, "Working, returned valid status (" + status + ")"));
            } else {
                logger.error("Test FAIL for {} with {} - HTTP 200, status={}, time={}ms", api, service, status, time);
                instance.addResult(new TestResult(service, false, "Invalid cobalt status (" + status + ")"));
            }
        } else {
            // if we didn't get back a 200 response, it failed
            String errorMessage;
            // there SHOULD be an error message, so parse it
            // cobalt 7 vs. 10 sends it back differently
            if (jsonResponse.has("error")) {
                JSONObject errorBody = jsonResponse.getJSONObject("error");
                errorMessage = errorBody.getString("code");
            } else if (jsonResponse.has("text")) {
                errorMessage = jsonResponse.getString("text");
            } else {
                errorMessage = "Unknown error, could not parse error from API";
            }

            // older cobalt shows error messages with HTML, parse it out
            errorMessage = Jsoup.parse(errorMessage).text();
            // if we got rate limited, rerun the test in a few seconds
            if (status.equalsIgnoreCase("rate-limit") || errorMessage.contains("rate_exceeded")) {
                // we maxed out the attempts for us to care
                if (attempts >= 5) {
                    logger.error("Test FAIL for {} with {} - attempts limit REACHED with {} tries, time={}ms", api, service, attempts, time);
                    instance.addResult(new TestResult(service, false, "Rate limited, max attempts reached (5)"));
                    return;
                }
                // retry again, but randomize the time to prevent more rate limits
                Random rand = new Random();
                int secondsToWait = rand.nextInt(20 - 10 + 1) + 10;
                logger.warn("Test RATE-LIMITED for {} with {} attempts={}, time={}ms - trying again in {} seconds", api, service, attempts, time, secondsToWait);
                try {
                    Thread.sleep(secondsToWait * 1000);
                    runApiTest();
                } catch (InterruptedException interruptedException) {
                    logger.error("Rate-limit retry interrupted for {} with {}", api, service, interruptedException);
                    instance.addResult(new TestResult(service, false, interruptedException.toString()));
                    return;
                }
                return;
            }
            // test failed for xyz reason
            // this is a regular cobalt fail
            logger.error("Test FAIL for {} with {} - HTTP {}, status=error, reason={}, time={}ms", api, service, responseCode, errorMessage, time);
            instance.addResult(new TestResult(service, false, errorMessage));
        }
    }

    /**
     * Check the headers from the tunnel url.
     *
     * @param tunnelUrl The tunnel url to check,
     * @param status    The status in the response.
     * @param time      The time it took for the request.
     */
    private void checkHeaders(String tunnelUrl, String status, long time) {
        ContentLengthHeader checkTunnelLength = RequestUtil.checkTunnelLength(tunnelUrl);
        // there were no content-length/estimated-content-length header
        if (checkTunnelLength == null) {
            // for YouTube, anything without the proper headers is failure, most of the time...?
            if (service.toLowerCase(Locale.ROOT).contains("youtube")) {
                logger.error("Test FAILED for {} with {} - HTTP 200, status={}, time={}ms - youtube missing content-length header", api, service, status, time);
                instance.addResult(new TestResult(service, false, "Not working, didn't respond with proper content-length header"));
            } else {
                logger.warn("Test PASS for {} with {} - HTTP 200, status={}, time={}ms - missing content-length header", api, service, status, time);
                instance.addResult(new TestResult(service, true, "Working, returned valid status, but no content-length header to verify"));
            }
            return;
        }

        // get the header in the response
        long size = checkTunnelLength.size();
        String header = checkTunnelLength.header();
        // headers returned valid length
        if (size > 1000) {
            logger.info("Test PASS for {} with {} - HTTP 200, status={}, time={}ms, size={}, header={}", api, service, status, time, size, header);
            instance.addResult(new TestResult(service, true, "Working, returned valid status, and has valid " + header + " header"));
            return;
        }
        // headers reported 0 content length, which means it failed
        if (size == 0) {
            logger.error("Test FAIL for {} with {} - HTTP 200, status={}, time={}ms, size={}, header={}", api, service, status, time, size, header);
            instance.addResult(new TestResult(service, false, "Not working as " + header + " is 0"));
            return;
        }
        // header length is too small for content
        if (size < 1000) {
            logger.error("Test FAIL for {} with {} - HTTP 200, status={}, time={}ms, size={}, header={} - too small", api, service, status, time, size, header);
            instance.addResult(new TestResult(service, false, "Not working as " + header + " is too small (" + size + ")"));
        }
    }

    @Override
    public String toString() {
        return api + ":" + service;
    }
}