(ns clj-auth-cache.auth-proxy
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.core.cache :as cache]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]))

;; Кэш для хранения данных
(def data-cache (atom (cache/ttl-cache-factory {:ttl 3600})))

;; Функция для обращения к auth сервису и получения токена
(defn get-auth-token [username password auth-url]
  (let [url (str auth-url "/auth/userauth")
        body (json/generate-string {:userName username :password password})
        response (http/post {:url url :body body :content-type :json})]
    response))

;; Функция для добавления HTTP-заголовков к ответу
(defn add-cache-headers [response]
  (response/assoc (response/header response "Cache-Control" "public, max-age=3600")))

;; Основная функция для обработки HTTP-запросов
(defn app [request]
  (let [params (:params request)
        username (get params "userName")
        password (get params "password")
        auth-url "http://your-auth-service-url"]
    (if (and username password)
      (let [token-response (get-auth-token username password auth-url)]
        (if (= 200 (:status token-response))
          (let [body-json (json/read-str true (:body token-response))
                cached-data {:clientIdentifier (:clientIdentifier body-json)
                             :userName (:userName body-json)
                             :userBranch (:userBranch body-json)
                             :systemDate (:systemDate body-json)}]
            (swap! data-cache cache/assoc username cached-data)
            (add-cache-headers {:status 200
                                :headers {"Content-Type" "application/json"}
                                :body (:body token-response)}))
          (add-cache-headers {:status 400
                              :body "Authentication failed. Invalid username or password."})))
      (add-cache-headers {:status 400
                          :body "Bad Request. Provide both username and password."})))

;; Функция для старта веб-сервера
(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (jetty/run-jetty (wrap-params app) {:port port})))

;; Запуск веб-сервера при старте приложения
(-main)
