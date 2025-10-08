package lol.hyper.cobaltdirectory.utils;

import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private static final Pattern TUNNEL = Pattern.compile("^(https?)://([^/]+)(/(?:tunnel|api/stream).*)$", Pattern.CASE_INSENSITIVE);

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
}
