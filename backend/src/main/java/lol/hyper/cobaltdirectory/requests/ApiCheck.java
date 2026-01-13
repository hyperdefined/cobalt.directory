package lol.hyper.cobaltdirectory.requests;

import lol.hyper.cobaltdirectory.CobaltDirectory;
import lol.hyper.cobaltdirectory.instance.Instance;
import lol.hyper.cobaltdirectory.utils.RequestUtil;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiCheck {

    private final Instance instance;
    private final Logger logger = LogManager.getLogger(ApiCheck.class, CobaltDirectory.getMessageFactory());

    public ApiCheck(Instance instance) {
        this.instance = instance;
    }

    public void run() {
        String protocol = instance.getProtocol();
        String api = instance.getApi();

        logger.info("Checking API status for {}", api);
        String requestApi = protocol + "://" + api;
        char lastChar = requestApi.charAt(requestApi.length() - 1);
        // remove last / if it exists
        if (lastChar == '/') {
            requestApi = requestApi.substring(0, requestApi.length() - 1);
        }

        // check API
        if (!RequestUtil.head(requestApi)) {
            logger.error("{} failed HEAD request, marking instance as offline!", api);
            instance.setOffline();
            return;
        }

        RequestResults apiContent = RequestUtil.requestJSON(requestApi);
        String responseContent = apiContent.responseContent();
        if (responseContent == null) {
            logger.error("responseContent returned null for {}", requestApi);
            instance.setOffline();
            return;
        }
        JSONObject json;
        try {
            json = new JSONObject(responseContent);
        } catch (JSONException exception) {
            logger.error("Failed to parse JSON for {}", requestApi, exception);
            instance.setOffline();
            return;
        }

        logger.info("Found valid cobalt API under {}", requestApi);
        instance.setApiWorking(true);
        // on cobalt 10, the JSON response is different
        if (json.has("cobalt")) {
            loadNewApi(json);
            return;
        }

        if (json.has("version")) {
            String version = json.getString("version");
            // older instances had -dev in the version
            if (version.contains("-dev")) {
                version = version.replace("-dev", "");
            }
            instance.setVersion(StringEscapeUtils.escapeHtml4(version));
        } else {
            logger.warn("{} is online, but failed to get version", api);
            instance.setOffline();
            return;
        }
        if (json.has("startTime")) {
            String startTimeString = String.valueOf(json.getLong("startTime"));
            if (startTimeString.matches("[0-9]+")) {
                instance.setStartTime(json.getLong("startTime"));
            } else {
                instance.setStartTime(0L);
                logger.warn("{} has an invalid startTime!", api);
            }
        }
    }

    private void loadNewApi(JSONObject response) {
        instance.setIs10(true);
        JSONObject cobalt = response.getJSONObject("cobalt");
        if (cobalt.has("version")) {
            String version = cobalt.get("version").toString();
            instance.setVersion(StringEscapeUtils.escapeHtml4(version));
        } else {
            instance.setVersion("Unknown");
        }

        if (cobalt.has("startTime")) {
            instance.setStartTime(cobalt.getLong("startTime"));
        }

        // some people remove this for no reason
        if (response.has("git")) {
            JSONObject git = response.getJSONObject("git");
            String remote = StringEscapeUtils.escapeHtml4(git.getString("remote"));
            instance.setRemote(remote);
            if (!remote.equalsIgnoreCase("imputnet/cobalt") && !remote.equalsIgnoreCase("wukko/cobalt")) {
                logger.warn("{} is running a FORK, remote is {}", instance.getApi(), remote);
                instance.setFork(true);
            } else {
                instance.setFork(false);
            }
        } else {
            logger.warn("{} is missing git information!", instance.getApi());
        }

        // on cobalt 10, check to see if the instance has turnstile on
        // if it's enabled, then mark all tests as fail since we can't send requests
        if (cobalt.has("turnstileSitekey")) {
            logger.warn("{} has turnstile!", instance.getApi());
            instance.setTurnstile(true);
        }
    }

    @Override
    public String toString() {
        return instance.getApi() + ":" + "check";
    }
}