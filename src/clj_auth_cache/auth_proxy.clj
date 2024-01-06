(ns clj-auth-cache.auth-proxy
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [clojure.core.cache :as cache]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [ring.util.response :as response]
            [clojure.tools.logging :as log]))

;; Кэш для хранения данных
(def data-cache (atom (cache/ttl-cache-factory {:ttl 3600})))

(defn get-auth-token [username password auth-url]
  (http/post (str auth-url "/userauth")
             {:body (json/generate-string {:userName username :password password})
              :content-type :json}))

(defn error-response
  [message]
  {:status 400 :body message})

;; Основная функция для обработки HTTP-запросов
(defn app [request]
  (let [params (:body request)
        username (get params "userName")
        password (get params "password")
        auth-url (or (System/getenv "AUTH_SERVICE_URL") "http://your-default-auth-service-url")]
    (log/info (str "Received request with raw parameters: " params))
    (log/info (str "Extracted username: " username " and password: " password))
    (log/info (str "Auth service URL: " auth-url))
    (if (and username password)
      (let [cached-token-response (get @data-cache username)
            token-response (or cached-token-response
                               (get-auth-token username password auth-url))]
        (if (= 200 (:status token-response))
          (do
            (when-not cached-token-response
              (swap! data-cache assoc username token-response))
            {:status 200 :headers {"Content-Type" "application/json"
                                   "Cache-Control" "public, max-age=3600"}
             :body (:body token-response)})
          (error-response
            "Authentication failed. Invalid username or password.")))
      (error-response
        "Bad Request. Provide both username and password."))))

(def handler (wrap-json-body app))

;; Функция для старта веб-сервера
(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (jetty/run-jetty #'handler {:port port :join? false})))
