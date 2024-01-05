# Используйте базовый образ Clojure
FROM clojure:openjdk-11-lein-2.9.6

# Установите рабочую директорию в /app
WORKDIR /app

# Скопируйте зависимости и код в образ
COPY project.clj .
COPY src/ src/

# Выполните lein deps, чтобы установить зависимости
RUN lein deps

# Определите переменные окружения JVM_OPTS
ENV JVM_OPTS="-Xms256m -Xmx512m"

# Запустите ваше приложение
CMD ["lein", "run"]
