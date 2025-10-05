package com.belhaddou.jroxy.util;

import com.belhaddou.jroxy.configuration.JRoxyConfig;

public class UrlUtils {

    /**
     * Construct url from Host Object
     *
     * @param selectedInstance
     * @return
     */

    public static String getUrl(JRoxyConfig.Host selectedInstance) {
        return selectedInstance.getAddress() + ":" + selectedInstance.getPort();
    }
}
