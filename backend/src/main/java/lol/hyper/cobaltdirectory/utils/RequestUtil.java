package lol.hyper.cobaltdirectory.utils;

import lol.hyper.cobaltdirectory.CobaltDirectory;
import lol.hyper.cobaltdirectory.requests.RequestResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUtil {

    public static final Logger logger = LogManager.getLogger(RequestUtil.class);
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);
    private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

    /**
     * Send a POST request.
     *
     * @param body The body to send.
     * @param url  The url to send to.
     * @return A RequestResults object.
     */
    public static RequestResults sendPost(JSONObject body, String url, String authorization) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", CobaltDirectory.USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));

        // use our API key if we have one
        if (authorization != null) {
            builder.header("Authorization", "Api-Key " + authorization);
        }

        HttpRequest request = builder.build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            Map<String, List<String>> rawHeaders = response.headers().map();
            HashMap<String, String> headers = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : rawHeaders.entrySet()) {
                headers.put(entry.getKey().toLowerCase(Locale.ROOT), String.join(", ", entry.getValue()));
            }

            return new RequestResults(response.body(), statusCode, headers, null);
        } catch (Exception e) {
            return new RequestResults(null, -1, new HashMap<>(), e);
        }
    }

    /**
     * Request a JSON object from URL.
     *
     * @param url The URL to request.
     * @return The RequestResults it returns. Returns NULL content if it failed.
     */
    public static RequestResults requestJSON(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", CobaltDirectory.USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            return new RequestResults(response.body(), statusCode, null, null);
        } catch (Exception exception) {
            logger.error("Unable to connect to or read from {}", url, exception);
            return new RequestResults(null, -1, null, exception);
        }
    }

    /**
     * Test a cobalt's frontend. It will match the HTML title "cobalt".
     *
     * @param url The url to test.
     * @return true/false if it works and is valid.
     */
    public static boolean testFrontEnd(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", CobaltDirectory.USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            Matcher matcher = TITLE_PATTERN.matcher(body);
            if (matcher.find()) {
                String title = matcher.group(1).trim();
                if (title.equalsIgnoreCase("cobalt")) {
                    return true;
                } else {
                    logger.warn("{} frontend is alive, but title does NOT match to cobalt. Please manually check this!", url);
                }
            } else {
                return false;
            }
        } catch (Exception exception) {
            logger.error("Unable to connect to or read from {}", url, exception);
            return false;
        }
        return false;
    }

    /**
     * Check the size of the length headers. cobalt sometimes reports it.
     * 0 means it failed.
     *
     * @param url The tunnel URL in cobalt's response.
     * @return The length.
     */
    public static long checkTunnelLength(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", CobaltDirectory.USER_AGENT)
                    .HEAD()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return extractLength(response);
        } catch (Exception exception) {
            logger.error("Unable to connect to or read from {}", url, exception);
            return -1;
        }
    }

    /**
     * Make a HEAD request to a given URL.
     *
     * @param url The url.
     * @return If the request was successful or not.
     */
    public static boolean head(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", CobaltDirectory.USER_AGENT)
                    .HEAD()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception exception) {
            logger.error("Unable to connect to or read from {}", url, exception);
            return false;
        }
    }

    /**
     * Request a site's HTML.
     *
     * @param url       The url.
     * @param userAgent The user agent.
     * @return RequestResults containing the results.
     */
    public static RequestResults request(String url, String userAgent) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", userAgent)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            return new RequestResults(body, response.statusCode(), null, null);
        } catch (Exception exception) {
            logger.error("Unable to connect to or read from {}", url, exception);
            return new RequestResults(null, -1, null, null);
        }
    }

    /**
     * Check headers for content-length or estimated-content-length.
     *
     * @param response The HttpResponse.
     * @return The content-length size, or -1 if it's not there.
     */
    private static long extractLength(HttpResponse<?> response) {
        return firstNonNegative(response.headers().firstValue("content-length").orElse(null))
                .orElseGet(() -> firstNonNegative(response.headers().firstValue("estimated-content-length").orElse(null))
                        .orElse(-1L));
    }

    private static OptionalLong firstNonNegative(String value) {
        if (value == null) return OptionalLong.empty();
        try {
            long n = Long.parseLong(value.trim());
            return n >= 0 ? OptionalLong.of(n) : OptionalLong.empty();
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }
}
