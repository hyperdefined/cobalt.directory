package lol.hyper.cobaltdirectory.instance;

import lol.hyper.cobaltdirectory.tests.TestResult;

import java.util.ArrayList;
import java.util.List;

public class Instance implements Comparable<Instance> {

    private final String frontEnd;
    private final String api;
    private final String protocol;
    private String version;
    private long startTime;
    private boolean apiWorking;
    private boolean is10;
    private boolean turnstile = false;
    private boolean fork;
    private String remote;

    private final List<TestResult> testResults = new ArrayList<>();

    public Instance(String frontEnd, String api, String protocol) {
        this.frontEnd = frontEnd;
        this.api = api;
        this.protocol = protocol;
    }

    public String toString() {
        return this.api;
    }

    public String getApi() {
        return api;
    }

    public String getFrontEnd() {
        return frontEnd;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getVersion() {
        return version;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isApiWorking() {
        return apiWorking;
    }

    public void setApiWorking(boolean apiWorking) {
        this.apiWorking = apiWorking;
    }

    public boolean is10() {
        return is10;
    }

    public void setIs10(boolean is10) {
        this.is10 = is10;
    }

    public void setTurnstile(boolean turnstile) {
        this.turnstile = turnstile;
    }

    public boolean hasTurnstile() {
        return turnstile;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }

    public void addResult(TestResult testResult) {
        testResults.add(testResult);
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public void setOffline() {
        this.setApiWorking(false);
        this.setVersion("Offline");
    }

    @Override
    public int compareTo(Instance instance) {
        return this.api.compareTo(instance.api);
    }
}
