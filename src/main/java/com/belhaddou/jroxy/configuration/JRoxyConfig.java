package com.belhaddou.jroxy.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "proxy")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JRoxyConfig {

    private String retries;
    private String defaultLoadBalancing;
    private Listen listen;
    @Builder.Default
    private List<Services> services = new ArrayList<>();

    @Data
    public static class DefaultLoadBalancing {
        private String strategy;
    }

    @Data
    public static class Listen {
        private String address;
        private int port;
    }

    @Data
    public static class Services {
        private String name;
        private String domain;
        private String loadBalancer;
        private List<Host> hosts;
    }

    @Data
    public static class Host {
        private String address;
        private int port;
    }
}
