package lol.hyper.cobaltdirectory.tests;

import lol.hyper.cobaltdirectory.requests.RequestResults;
import lol.hyper.cobaltdirectory.utils.RequestUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XiaohongshuTest {

    private static final Pattern VIDEO_PATTERN = Pattern.compile("/explore\\/[a-f0-9]{24}\\?xsec_token=[^&]+(?:&amp;|&)xsec_source=");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0";

    public static String getTestUrl() {
        RequestResults result = RequestUtil.request("https://www.xiaohongshu.com/explore", USER_AGENT);
        if (result.responseCode() != 200) {
            return null;
        }

        String content = result.responseContent();
        Matcher matcher = VIDEO_PATTERN.matcher(content);
        if (matcher.find()) {
            return "https://www.xiaohongshu.com" + matcher.group(0);
        }
        return null;
    }
}
