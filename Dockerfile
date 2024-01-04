# Dockerfile
FROM clojure:openjdk-11

WORKDIR /app

COPY project.clj /app/
RUN lein deps

COPY . /app

# Pass CACHE_TTL_SECONDS as an environment variable with a default value of 3600
ENV CACHE_TTL_SECONDS=3600

CMD ["lein", "run"]
