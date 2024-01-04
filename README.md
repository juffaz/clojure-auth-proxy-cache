# Clojure Auth Proxy(clojure-auth-proxy-cache)

This is a simple Clojure application designed to cache tokens obtained from an authorization service's REST API, reducing the frequency of requests.

## Functionality

- Obtaining tokens using caching to minimize requests to the authorization service.

## Usage

1. **Build Docker Image:**

   ```bash
   docker build -t clojure-auth-proxy .

Run Docker Container:

bash

docker run -p 8080:8080 -e AUTH_SERVICE_URL=http://your-auth-service-url -e CACHE_TTL_SECONDS=10800 clojure-auth-proxy

The application will be available at http://localhost:8080.

Customize the AUTH_SERVICE_URL and CACHE_TTL_SECONDS environment variables to specify the URL of the authorization service and the cache TTL, respectively.

Example Usage in Code:
