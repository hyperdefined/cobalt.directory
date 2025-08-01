package lol.hyper.cobaltdirectory.utils;

import lol.hyper.cobaltdirectory.CobaltDirectory;
import lol.hyper.cobaltdirectory.requests.RequestResults;
import lol.hyper.cobaltdirectory.tests.ContentLengthHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RequestUtil {

    public static final Logger logger = LogManager.getLogger(RequestUtil.class);
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);

    /**
     * Send a POST request.
     *
     * @param body The body to send.
     * @param url  The url to send to.
     * @return A RequestResults object.
     */
    public static RequestResults sendPost(JSONObject body, String url, String authorization) {
        String content;
        HttpURLConnection connection = null;
        int responseCode = -1;
        HashMap<String, String> headers = new HashMap<>();

        try {
            StringBuilder stringBuilder;
            BufferedReader reader;
            URI urlFixed = new URI(url);
            connection = (HttpURLConnection) urlFixed.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            if (authorization != null) {
                connection.setRequestProperty("Authorization", "Api-Key " + authorization);
            }
            connection.setRequestProperty("User-Agent", CobaltDirectory.getUserAgent());
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);

            byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream stream = connection.getOutputStream();
            stream.write(out);
            stream.close();

            responseCode = connection.getResponseCode();

            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                String key = entry.getKey();
                String value = String.join(", ", entry.getValue());
                if (key != null) {
                    headers.put(key.toLowerCase(Locale.ROOT), value);
                }
            }

            InputStream inputStream;
            if (responseCode == 200) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            String line;
            reader = new BufferedReader(new InputStreamReader(inputStream));
            stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            content = stringBuilder.toString();

        } catch (Exception exception) {
            logger.error("Unable to send post", exception);
            return new RequestResults(null, responseCode, headers, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new RequestResults(content, responseCode, headers, null);
    }

    /**
     * Request a JSON object from URL.
     *
     * @param url The URL to request.
     * @return The RequestResults it returns. Returns NULL content if it failed.
     */
    public static RequestResults requestJSON(String url) {
        String rawJSON;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestProperty("User-Agent", CobaltDirectory.getUserAgent());
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.connect();

            InputStream in;
            if (connection.getResponseCode() >= 400) {
                in = connection.getErrorStream();
            } else {
                in = connection.getInputStream();
            }

            if (in == null) {
                connection.disconnect();
                return new RequestResults(null, -1, null, null);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (Exception exception) {
            logger.error("Unable to connect to or read from {}", url, exception);
            return new RequestResults(null, -1, null, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        if (rawJSON.isEmpty()) {
            logger.error("Read JSON from {} returned an empty string!", url);
            return new RequestResults(null, -1, null, null);
        }
        return new RequestResults(rawJSON, -1, null, null);
    }

    /**
     * Test a cobalt's frontend. It will match the HTML title "cobalt".
     *
     * @param url The url to test.
     * @return true/false if it works and is valid.
     */
    public static boolean testFrontEnd(String url) {
        int response;
        HttpURLConnection connection = null;
        try {
            URI connectUrl = new URI(url);
            connection = (HttpURLConnection) connectUrl.toURL().openConnection();
            connection.setRequestProperty("User-Agent", CobaltDirectory.getUserAgent());
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.connect();
            response = connection.getResponseCode();

            if (response == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                Matcher matcher = TITLE_PATTERN.matcher(content.toString());
                if (matcher.find()) {
                    String title = matcher.group(1).trim();
                    if (title.equalsIgnoreCase("cobalt")) {
                        return true;
                    } else {
                        logger.warn("{} frontend is alive, but title does NOT match to cobalt. Please manually check this!", url);
                    }
                }
            } else {
                return false;
            }
        } catch (Exception exception) {
            logger.error("Unable to read URL {}", url, exception);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }

    /**
     * Returns the status code of a given URL.
     *
     * @param urlString The URL to test.
     * @return The status code.
     */
    public static RequestResults getStatusCode(String urlString) {
        int statusCode;
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(urlString).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", CobaltDirectory.getUserAgent());
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.connect();
            statusCode = connection.getResponseCode();
        } catch (IOException exception) {
            logger.error("Unable to test url {}", urlString, exception);
            return new RequestResults(null, -1, null, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new RequestResults(null, statusCode, null, null);
    }

    /**
     * Check the size of the length headers. cobalt sometimes reports it.
     * 0 means it failed.
     *
     * @param urlString The tunnel URL in cobalt's response.
     * @return A Headers record with the header name and its value, or null if not present.
     */
    public static ContentLengthHeader checkTunnelLength(String urlString) {
        HttpURLConnection connection = null;
        try {
            URI connectUrl = new URI(urlString);
            connection = (HttpURLConnection) connectUrl.toURL().openConnection();
            connection.setRequestProperty("User-Agent", CobaltDirectory.getUserAgent());
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.connect();
            return extractLength(connection);
        } catch (Exception exception) {
            logger.error("Unable to read URL {}", urlString, exception);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Make a HEAD request to a given URL.
     *
     * @param url The url.
     * @return If the request was successful or not.
     */
    public static boolean head(String url) {
        HttpURLConnection connection = null;
        try {
            URI connectUrl = new URI(url);
            connection = (HttpURLConnection) connectUrl.toURL().openConnection();
            connection.setRequestProperty("User-Agent", CobaltDirectory.getUserAgent());
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.connect();

            return connection.getResponseCode() == 200;
        } catch (Exception exception) {
            logger.error("Unable to HEAD URL {}", url, exception);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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
        String content;
        HttpURLConnection connection = null;
        int code;
        try {
            connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.connect();

            InputStream in;
            if (connection.getResponseCode() >= 400) {
                in = connection.getErrorStream();
            } else {
                in = connection.getInputStream();
            }

            if (in == null) {
                connection.disconnect();
                return new RequestResults(null, -1, null, null);
            }

            code = connection.getResponseCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (Exception exception) {
            logger.error("Unable to connect to or read from {}", url, exception);
            return new RequestResults(null, -1, null, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        if (content.isEmpty()) {
            logger.error("Read content from {} returned an empty string!", url);
            return new RequestResults(null, -1, null, null);
        }
        return new RequestResults(content, code, null, null);
    }

    /**
     * Check headers for content-length or estimated-content-length.
     *
     * @param connection The HttpURLConnection.
     * @return A Headers record with the header name and its value, or null if not present.
     */
    private static ContentLengthHeader extractLength(HttpURLConnection connection) {
        String contentLength = connection.getHeaderField("content-length");
        String estimatedLength = connection.getHeaderField("estimated-content-length");

        long contentSize = -1;
        long estimatedSize = -1;

        if (contentLength != null) {
            try {
                contentSize = Long.parseLong(contentLength);
            } catch (NumberFormatException exception) {
                logger.error("Unable to parse content-length {}", estimatedLength, exception);
                return null;
            }
        }

        if (estimatedLength != null) {
            try {
                estimatedSize = Long.parseLong(estimatedLength);
            } catch (NumberFormatException exception) {
                logger.error("Unable to parse estimated-content-length {}", estimatedLength, exception);
                return null;
            }
        }

        boolean contentValid = contentSize >= 0;
        boolean estimatedValid = estimatedSize >= 0;

        if (contentValid && estimatedValid) {
            return (contentSize >= estimatedSize)
                    ? new ContentLengthHeader("content-length", contentSize)
                    : new ContentLengthHeader("estimated-content-length", estimatedSize);
        } else if (contentValid) {
            return new ContentLengthHeader("content-length", contentSize);
        } else if (estimatedValid) {
            return new ContentLengthHeader("estimated-content-length", estimatedSize);
        }

        return null;
    }
}