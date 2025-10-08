# JRoxy

_A Java Reverse Proxy with Caching and Load Balancing._

In this repository there is the source code of Jroxy and also helm chart to deploy it (helm/jroxy), for demo purpose another client application chart is included to test traffic (helm/client).
## Overview

**JRoxy** is a Java-based reverse proxy designed to route HTTP traffic efficiently to downstream services. It provides
built-in service discovery, load balancing, and caching capabilities configured through a simple YAML file.

---

## Core Features

### 1. Configuration Loading

Upon startup, JRoxy:

- Reads the YAML configuration file.
- Registers all downstream services and their instances (host and port) into a **Service Registry**.
- Makes the registry accessible to other components such as the **LoadBalancer** and **Forwarding Service**.

---

### 2. Request Handling

When a request is received:

1. JRoxy determines the target downstream service using the `Host` header.
2. It queries the **Service Registry** for available instances of that service.
3. The **LoadBalancer** selects an instance based on the configured load-balancing strategy.
4. The request is then proxied to the selected instance.

---

### 3. Caching
EhCache is used as in-memory cache in this application, it can be easily switched to another caching tool like redis by creating a new implementation to the interface `EhCacheService` and configuration. 
- **GET requests** are cached:
    - A unique cache key is built from the request.
    - If a valid cached response exists, it’s returned immediately.
    - Otherwise, the request is forwarded downstream, and the response is cached for future use.
- **Non-GET requests** (e.g., POST, PUT, DELETE) bypass the cache and are sent directly to the downstream service.

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
  Implement periodic and startup-time health checks for downstream instances to dynamically update their availability in
  the Service Registry.
- **Chain of Responsibility**

  Create and configure a chain of processors to which the request will be submitted for validation and security
  filtering

---
## Application architecture

![img.png](https://private-user-images.githubusercontent.com/39200728/499093166-153a0980-cbbe-481e-8a8f-a06e39d5618d.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NTk5NjI2MTYsIm5iZiI6MTc1OTk2MjMxNiwicGF0aCI6Ii8zOTIwMDcyOC80OTkwOTMxNjYtMTUzYTA5ODAtY2JiZS00ODFlLThhOGYtYTA2ZTM5ZDU2MThkLnBuZz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMDglMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDA4VDIyMjUxNlomWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPWUxNDQ4ZDUxNGE0NmM1YThmMzk3YTUwZmNiOGVmOTRiN2ViODFkZTE5NzgzOGZiNjRkZjExYmEzOTBiZmU0ODQmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.IiiaWQ8YE52q1fnkgo06v2sM-qo0p-tM_PSTFcoFrdk)

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
ref: https://minikube.sigs.k8s.io/docs/handbook/accessing/
    - **macOS:**
      ```bash
      minikube service jroxy-service --url # result example: http://127.0.0.1:64060
      ```
6. Start making requests to : http://my-service.my-company.com:64060/ with minukube opened port.

---

## References

- Cloudflare: [What is Cache-Control?](https://www.cloudflare.com/en-gb/learning/cdn/glossary/what-is-cache-control/)
- Minikube: [Accessing apps
  ](https://minikube.sigs.k8s.io/docs/handbook/accessing/) 
