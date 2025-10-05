package com.belhaddou.jroxy.model;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class InstanceWithHealth {
    JRoxyConfig.Host host;
    Boolean healthy;
}
