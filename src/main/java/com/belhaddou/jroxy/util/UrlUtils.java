package com.belhaddou.jroxy.util;

public class UrlUtils {
    /**
     *
     * @param host
     * @return
     */

    public static String extractSubdomain(String host, String baseHost) {
        if (host == null) return "";
        String cleanHost = host.split(":")[0]; // remove port
        if (cleanHost.contains("." + baseHost)) {
            return cleanHost.substring(0, cleanHost.indexOf("." + baseHost));
        }
        return "";
    }
}
