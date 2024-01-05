# Используйте базовый образ Clojure
FROM clojure:openjdk-11-lein-2.9.6

# Установите Leiningen
RUN curl -o /usr/local/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
    && chmod +x /usr/local/bin/lein

# Задайте переменную окружения LEIN_ROOT
ENV LEIN_ROOT 1

# Установите рабочую директорию в /app
WORKDIR /app

# Скопируйте зависимости и код в образ
COPY project.clj .
COPY src/ src/
COPY resources/ resources/

# Выполните lein deps, чтобы установить зависимости
RUN lein deps

# Определите переменные окружения JVM_OPTS
ENV JVM_OPTS="-Xms256m -Xmx512m"

# Запустите ваше приложение
CMD ["lein", "run"]


