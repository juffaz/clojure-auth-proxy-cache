# Dockerfile
####FROM clojure:openjdk-11
FROM clojure:openjdk-11-lein-2.9.8-bullseye

# Устанавливаем Leiningen
#RUN curl -o /usr/local/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && \
#    chmod +x /usr/local/bin/lein && \
#    lein

WORKDIR /app

COPY project.clj /app/
RUN lein deps

COPY . /app

# Передаем CACHE_TTL_SECONDS как переменную окружения со значением по умолчанию 3600
ENV CACHE_TTL_SECONDS=3600

CMD ["lein", "run"]


