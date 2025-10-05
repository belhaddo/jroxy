package com.belhaddou.jroxy.service.registry;

import com.belhaddou.jroxy.model.InstanceWithHealth;

import java.util.List;
import java.util.Map;

public interface ServiceRegistry {
    Map<String, List<InstanceWithHealth>> getRegistry();
}
