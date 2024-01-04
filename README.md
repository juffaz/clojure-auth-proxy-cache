# Clojure Auth Proxy(clojure-auth-proxy-cache)

This is a simple Clojure application designed to cache tokens obtained from an authorization service's REST API, reducing the frequency of requests.

## Functionality

- Obtaining tokens using caching to minimize requests to the authorization service.

## Usage

1. **Build Docker Image:**

   ```bash
   docker build -t clojure-auth-proxy .

   

2. **Run Docker Container:**

   ```bash
   docker run -p 8080:8080 -e AUTH_SERVICE_URL=http://your-auth-service-url -e CACHE_TTL_SECONDS=10800 clojure-auth-proxy

3. **Run from my Docker Hub Registry:**

   ```bash
   docker run -p 8080:8080 -e AUTH_SERVICE_URL=http://your-auth-service-url -e CACHE_TTL_SECONDS=10800 yuvenaliyt/clojure-auth-proxy-cache:latest   

The application will be available at http://localhost:8080.

Customize the AUTH_SERVICE_URL and CACHE_TTL_SECONDS environment variables to specify the URL of the authorization service and the cache TTL, respectively.

## Contributing

If you wish to contribute to the project, please read CONTRIBUTING.md for additional information.

## License

This project is licensed under the MIT License.
**LICENSE:** 


