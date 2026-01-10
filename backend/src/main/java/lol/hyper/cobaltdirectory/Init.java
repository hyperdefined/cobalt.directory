package lol.hyper.cobaltdirectory;

import lol.hyper.cobaltdirectory.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.util.List;

public class Init {

    private final Logger logger = LogManager.getLogger(this);
    private String userAgent;
    private JSONObject tests;
    private JSONObject apiKeys;
    private List<String> instanceFileContents;
    private boolean proxy;
    private String proxyHost;
    private int proxyPort;

    public void start(String[] args) {
        // load the git information
        String commit = null;
        try {
            commit = getCommit();
        } catch (IOException | GitAPIException exception) {
            logger.error("Unable to get git repo information! Did you clone this correctly?", exception);
        }

        if (commit == null) {
            logger.error("Unable to get git repo information! Did you clone this correctly?");
            commit = "Unknown";
        }

        // set up the user agent
        logger.info("cobaltdirectory starting up.");
        logger.info("cobaltdirectory running commit: {}", commit);
        userAgent = "cobaltdirectory-git-<commit> (+https://cobalt.directory)".replace("<commit>", commit);

        // Output how many threads we can use
        int availableThreads = Runtime.getRuntime().availableProcessors();
        logger.info("Total available threads: {}", availableThreads);

        // parse the args
        String instanceFile = "instances"; // this is the default
        for (String arg : args) {
            String[] parts = arg.split("=", 2);
            String key = parts[0];
            String value = parts[1];
            if (key.equalsIgnoreCase("instances")) {
                instanceFile = value;
            }
        }

        // load files
        setupFiles(instanceFile);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public JSONObject getApiKeys() {
        return apiKeys;
    }

    public JSONObject getTests() {
        return tests;
    }

    public List<String> getInstanceFileContents() {
        return instanceFileContents;
    }

    /**
     * Gets the commit this is running.
     *
     * @return The commit hash, down to the first 6 characters.
     * @throws IOException     If there were any IO issues.
     * @throws GitAPIException If there were any issues reading the git information.
     */
    private String getCommit() throws IOException, GitAPIException {
        File root = new File("../.git");
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        try (Repository repository = repositoryBuilder.setGitDir(root).readEnvironment().findGitDir().build()) {
            if (repository == null) {
                logger.error("Unable to find git information. Did you fork this correctly?");
                return null;
            }
            try (Git git = new Git(repository)) {
                RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();
                String fullCommitID = latestCommit.getId().getName();
                return fullCommitID.substring(0, 7);
            }
        }
    }

    private void setupFiles(String instanceFile) {
        // load some files
        File instancesFile = new File(instanceFile);
        File testUrlsFile = new File("tests.json");
        File apiKeysFile = new File("apikeys.json");
        File proxyFile = new File("proxy.json");
        logger.info("Using instances file: {}", instancesFile.getAbsolutePath());
        logger.info("Using tests file: {}", testUrlsFile.getAbsolutePath());
        logger.info("Using api keys file: {}", apiKeysFile.getAbsolutePath());
        logger.info("Using proxy file: {}", proxyFile.getAbsolutePath());
        instanceFileContents = FileUtil.readRawFile(instancesFile);
        if (instanceFileContents.isEmpty()) {
            logger.error("{} exists, but it's empty?", instanceFile);
            System.exit(1);
        }
        String testUrlContents = FileUtil.readFile(testUrlsFile);
        String apiKeyContents = FileUtil.readFile(apiKeysFile);
        if (testUrlContents == null) {
            logger.error("tests.json failed to load! Unable to continue. Make sure tests.json exists!");
            System.exit(1);
        }
        if (apiKeyContents == null) {
            logger.info("apikeys.json is missing, making example file");
            JSONObject exampleApiKeys = new JSONObject();
            exampleApiKeys.put("api.domain.com", "api-key");
            FileUtil.writeFile(exampleApiKeys.toString(4), new File("apikeys.json"));
            apiKeyContents = "{}";
        }
        tests = new JSONObject(testUrlContents);
        if (tests.isEmpty()) {
            logger.error("tests.json exists, but it's empty?");
            System.exit(1);
        }
        apiKeys = new JSONObject(apiKeyContents);

        String proxyFileContents = FileUtil.readFile(proxyFile);
        if (proxyFileContents == null) {
            logger.warn("proxy.json failed to load! Can't use a proxy for requests.");
            proxy = false;
        } else {
            JSONObject proxyJson = new JSONObject(proxyFileContents);
            proxyHost = proxyJson.getString("host");
            proxyPort = Integer.parseInt(proxyJson.getString("port"));
            logger.info("Using proxy {}:{} for requests", proxyHost, proxyPort);
            proxy = true;

            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
            System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");

            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    if (getRequestorType() == RequestorType.PROXY) {
                        return new PasswordAuthentication(
                                proxyJson.getString("username"),
                                proxyJson.getString("password").toCharArray()
                        );
                    }
                    return null;
                }
            });
        }

        // folders for web
        File serviceFolder = new File("../web", "service");
        File instanceFolder = new File("../web", "instance");
        try {
            if (!serviceFolder.exists()) {
                Files.createDirectory(serviceFolder.toPath());
                logger.info("Creating folder for web: {}", serviceFolder.getAbsolutePath());
            }
            if (!instanceFolder.exists()) {
                Files.createDirectory(instanceFolder.toPath());
                logger.info("Creating folder for web: {}", instanceFolder.getAbsolutePath());
            }
        } catch (IOException exception) {
            logger.error("Unable to make folder for web!", exception);
        }
    }

    public boolean useProxy() {
        return proxy;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }
}
