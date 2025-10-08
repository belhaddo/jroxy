# JRoxy

_A Java Reverse Proxy with Caching and Load Balancing_

## Overview

**JRoxy** is a Java-based reverse proxy designed to route HTTP traffic efficiently to upstream services. It provides
built-in service discovery, load balancing, and caching capabilities configured through a simple YAML file.

---

## Core Features

### 1. Configuration Loading

Upon startup, JRoxy:

- Reads the YAML configuration file.
- Registers all upstream services and their instances (host and port) into a **Service Registry**.
- Makes the registry accessible to other components such as the **LoadBalancer** and **Forwarding Service**.

---

### 2. Request Handling

When a request is received:

1. JRoxy determines the target upstream service using the `Host` header.
2. It queries the **Service Registry** for available instances of that service.
3. The **LoadBalancer** selects an instance based on the configured load-balancing strategy.
4. The request is then proxied to the selected instance.

---

### 3. Caching

- **GET requests** are cached:
    - A unique cache key is built from the request.
    - If a valid cached response exists, it’s returned immediately.
    - Otherwise, the request is forwarded upstream, and the response is cached for future use.
- **Non-GET requests** (e.g., POST, PUT, DELETE) bypass the cache and are sent directly to the upstream service.

Caching behavior is inspired
by [Cloudflare’s Cache-Control guide](https://www.cloudflare.com/en-gb/learning/cdn/glossary/what-is-cache-control/).

---

### 4. Service Registry

The **Service Registry**:

- Loads and stores service definitions and instance metadata from YAML.
- Maintains instance health information in a thread-safe `ConcurrentHashMap`.
- Exposes available instances to the **LoadBalancer** and **Forwarding Service**.

---

### 5. Load Balancing

JRoxy supports multiple load-balancing strategies:

- **Random Strategy** (default)
- **Round Robin Strategy**

Custom strategies can be implemented by extending the `LoadBalancerStrategy` interface.

**Configuration Options:**

- Global default strategy:
  ```yaml
  proxy:
    default-load-balancing: "randomStrategy"
  ```
- Per-service override:
  ```yaml
  proxy:
    listen:
      address: "127.0.0.1"
      port: 8080
    services:
      - name: my-service
        domain: my-service.my-company.com
        load-balancer: "roundRobinStrategy" # Overrides global default
        hosts:
          - address: "127.0.0.1"
            port: 8081
          - address: "127.0.0.1"
            port: 8082
  ```

---

## Future Improvements

- **Throttling & Rate Limiting**  
  Prevent overload and protect against DoS attacks. (Token bucket, fixed window) algorithms

- **Distributed Caching**  
  Integrate external caches (Redis) for consistent caching across proxy instances and resilience during downtime.

- **Health Checks**  
  Implement periodic and startup-time health checks for upstream instances to dynamically update their availability in
  the Service Registry.
- **Chain of Responsibility**

  Create and configure a chain of processors to which the request will be submitted for validation and security
  filtering

---

## Setup Instructions

1. Add DNS mappings to `/etc/hosts`:
   ```
   127.0.0.1 my-service.my-company.com
   ```
2. Refresh DNS:
    - **macOS:**
      ```bash
      sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder
      ```
3. Minikube setup and setting up ingress controller
    - **macOS:**
      ```bash
      minikube start
      minikube addons enable ingress
      minikube addons enable ingress-dns
      ```
4. Install the applications
    - **macOS:**
      ```bash
      git clone git@github.com:belhaddo/jroxy.git
      cd cd jroxy/helm/
      helm install jroxy ./jroxy
      helm install client ./client
      ```
5. Open a tunnel from the local environment to k8s cluster
    - **macOS:**
      ```bash
      minikube service jroxy-service --url # result example: http://127.0.0.1:64060
      ```
6. Start making requests to : http://my-service.my-company.com:64060/ with minukube opened port.

---

## References

- Cloudflare: [What is Cache-Control?](https://www.cloudflare.com/en-gb/learning/cdn/glossary/what-is-cache-control/)
