package lol.hyper.cobaltdirectory.requests;

import java.util.HashMap;

public record RequestResults(String responseContent, int responseCode, HashMap<String, String> headers, Exception exception) { }