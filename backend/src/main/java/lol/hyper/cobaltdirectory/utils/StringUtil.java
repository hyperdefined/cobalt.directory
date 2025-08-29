package lol.hyper.cobaltdirectory.utils;

import lol.hyper.cobaltdirectory.instance.Instance;
import lol.hyper.cobaltdirectory.services.Services;
import lol.hyper.cobaltdirectory.tests.TestResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private static final Pattern TUNNEL = Pattern.compile("^(https?)://([^/]+)(/(?:tunnel|api/stream).*)$", Pattern.CASE_INSENSITIVE);

    /**
     * Make an instance table.
     *
     * @param instances The instances to use.
     * @param type      community/official
     * @return The HTML table.
     */
    public static String buildInstanceTable(List<Instance> instances, String type) {
        StringBuilder table = new StringBuilder();
        // build the table for output
        // frontend, api, version, services, score
        table.append("<div class=\"table-container\"><table id=\"sort-table\">\n<tr><th>Frontend</th><th>API</th><th>Version</th><th>Services</th><th>Score</th></tr>\n");

        List<Instance> filtered = instances.stream().filter(i -> {
                    String api = i.getApi();
                    if (api == null) return !type.equalsIgnoreCase("official");
                    return type.equalsIgnoreCase("official") == api.contains("imput.net");
                })
                .toList();

        // build each element for the table
        for (Instance instance : filtered) {
            String frontEndDisplay;
            String apiDisplay = instance.getApi();
            String version = instance.getVersion();
            String services;
            if (instance.isApiWorking()) {
                services = instance.getServiceCount(true) + "/" + instance.getTestResults().size();
                if (instance.getFrontEnd() == null) {
                    frontEndDisplay = "N/A";
                } else {
                    frontEndDisplay = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\">" + instance.getFrontEnd() + "</a>";
                }
                table.append("<tr class=\"").append(instance.getRating()).append("\">");
            } else {
                if (instance.getFrontEnd() == null) {
                    frontEndDisplay = "N/A";
                } else {
                    frontEndDisplay = instance.getFrontEnd();
                }
                services = "0/0";
                table.append("<tr class=\"offline\">");
            }
            table.append("<td>").append(frontEndDisplay).append("</td>");
            table.append("<td>").append(apiDisplay).append("</td>");
            table.append("<td>").append(version).append("</td>");
            table.append("<td>").append(services).append("</td>");
            // if the score is at least 0, that means we ran tests, link these tests
            if (instance.getScore() >= 0) {
                String score = Double.toString(instance.getScore()).split("\\.")[0] + "%";
                String scoreLink = "<a href=\"{{ site.url }}/instance/" + instance.getHash() + "\">" + score + "</a>";
                table.append("<td>").append(scoreLink).append("</td>");
            } else {
                // score was -1, which means we did not run any tests, so do not link the instance page
                table.append("<td>").append("Offline").append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</table></div>");
        return table.toString();
    }

    /**
     * Build working/not working services table for a given instance.
     *
     * @param instance The instance to use.
     * @return HTML table of working/not working services.
     */
    public static String buildScoreTable(Instance instance) {
        StringBuilder table = new StringBuilder();
        // build the table for output
        // service, status, status message
        table.append("<div class=\"table-container\"><table class=\"service-table\">\n<tr><th>Service</th><th>Working?</th><th>Status</th></tr>\n");

        // make it sort correctly
        instance.getTestResults().sort(Comparator.comparing(TestResult::service));

        for (TestResult result : instance.getTestResults()) {
            String service = result.service();
            String friendlyName = Services.getIdToFriendly().get(service);
            boolean working = result.status();
            String serviceLink = "<a href=\"{{ site.url }}/service/" + Services.makeSlug(service).replace("*", "") + "\">" + friendlyName + "</a>";
            table.append("<tr><td>").append(serviceLink).append("</td>");
            if (working) {
                table.append("<td>").append("✅").append("</td>").append("<td>").append(makeLogPretty(result.message())).append("</td>");
            } else {
                table.append("<td>").append("❌").append("</td>").append("<td>").append(makeLogPretty(result.message())).append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</table></div>");
        return table.toString();
    }

    /**
     * Build the table for a given service.
     *
     * @param instances The instances to use.
     * @param serviceId The id of the service name.
     * @param type      The type of instances we want.
     * @return A string of instances for this service.
     */
    public static String buildServiceTable(List<Instance> instances, String serviceId, String type) {
        List<Instance> filtered = new ArrayList<>(instances.stream().filter(i -> {
                    String api = i.getApi();
                    if (api == null) return !type.equalsIgnoreCase("official");
                    return type.equalsIgnoreCase("official") == api.contains("imput.net");
                })
                .toList());
        // Make them in alphabetical order
        Collections.sort(filtered);
        // Store which instance works with this service
        Map<Instance, Boolean> workingInstances = new HashMap<>();
        for (Instance instance : filtered) {
            boolean working = instance.getTestResults().stream().filter(testResult -> testResult.service().equals(serviceId)).map(TestResult::status).findFirst().orElse(false);
            workingInstances.put(instance, working);
        }

        StringBuilder table = new StringBuilder();
        // build the table for output
        // instance, status
        table.append("<div class=\"table-container\"><table id=\"sort-table\"><tr><th>Frontend</th><th>API</th><th>Working?</th><th>Status</th></tr>\n");

        for (Map.Entry<Instance, Boolean> pair : workingInstances.entrySet()) {
            Instance instance = pair.getKey();
            boolean working = pair.getValue();
            String frontEndDisplay;
            String apiDisplay = instance.getApi();
            String status;
            // if results is null, we did not run a test
            // this means the instance is offline
            TestResult results = instance.getTestResults().stream().filter(testResult -> testResult.service().equals(serviceId)).findFirst().orElse(null);
            if (results == null) {
                status = "Offline";
            } else {
                status = makeLogPretty(results.message());
            }
            if (instance.isApiWorking()) {
                if (instance.getFrontEnd() == null) {
                    frontEndDisplay = "N/A";
                } else {
                    frontEndDisplay = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\">" + instance.getFrontEnd() + "</a>";
                }
                table.append("<tr class=\"").append(instance.getRating()).append("\">");
            } else {
                if (instance.getFrontEnd() == null) {
                    frontEndDisplay = "N/A";
                } else {
                    frontEndDisplay = instance.getFrontEnd();
                }
                table.append("<tr class=\"offline\">");
            }
            table.append("<td>").append(frontEndDisplay).append("</td>");
            table.append("<td>").append(apiDisplay).append("</td>");
            if (working) {
                table.append("<td>").append("✅").append("</td>");
            } else {
                table.append("<td>").append("❌").append("</td>");
            }
            table.append("<td>").append(status).append("</td>");
            table.append("</tr>");
        }
        table.append("</table></div>");
        return table.toString();
    }

    /**
     * Generates the partial hash of a string.
     *
     * @param input The input.
     * @return Partial section of the hash. Used as an ID system.
     */
    public static String makeHash(String input) {
        String hash = DigestUtils.sha256Hex(input).toLowerCase(Locale.ROOT);
        return hash.substring(0, 10);
    }

    /**
     * Convert an error log from cobalt into a "pretty one."
     *
     * @param input The input log from cobalt.
     * @return The pretty version.
     */
    public static String makeLogPretty(String input) {
        if (input == null) {
            return "";
        }

        // java.net.SocketTimeoutException
        if (input.contains("SocketTimeout")) {
            return "Timed out (took over 20 seconds)";
        }
        // org.json.JSONException
        if (input.contains("JSONException")) {
            return "Failed to parse API response (invalid JSON)";
        }
        // it seems like this service is not supported yet or your link is invalid. have you pasted the right link?
        if (input.contains("link is invalid")) {
            return "Instance does not support this service";
        }
        // i don't see anything i could download by your link. try a different one!
        // i couldn't find anything about this link. check if it works and try again! some content may be region restricted, so keep that in mind.
        if (input.contains("i don't see anything") || input.contains("i couldn't find anything")) {
            return "No media found";
        }
        // something went wrong when i tried getting info about your link. are you sure it works? check if it does, and try again.
        if (input.contains("something went wrong")) {
            return "Instance failed to get media";
        }
        // couldn't get this youtube video because it requires an account to view. this limitation is done by google to seemingly stop scraping,
        // affecting all 3rd party tools and even their own clients. try again, but if issue persists, check the status page or create an issue on github.
        if (input.contains("an account to view")) {
            return "YouTube asked instance to login with an account";
        }
        // i couldn't get the temporary token that's required to download songs from soundcloud. try again, but if issue persists, check the status page or create an issue on github.
        if (input.contains("temporary token")) {
            return "Failed to get temporary token for Soundcloud";
        }
        return Jsoup.parse(input).text();
    }

    public static String rewrite(String tunnelUrl, String domain, String protocol) {
        if (tunnelUrl.contains("imput.net")) {
            return tunnelUrl;
        }
        Matcher m = TUNNEL.matcher(tunnelUrl);
        if (!m.matches()) {
            return tunnelUrl;
        }

        String scheme = (protocol != null) ? protocol : m.group(1);
        String host = (domain != null) ? domain : m.group(2);

        return m.replaceFirst(Matcher.quoteReplacement(scheme + "://" + host) + "$3");
    }

    public static String officialInstanceName(String api) {
        return api.replace(".imput.net", "");
    }
}
