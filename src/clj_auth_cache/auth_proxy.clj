(ns clj-auth-cache.auth-proxy
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.core.cache :as cache]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [ring.util.response :as response]
            [clojure.tools.logging :as log]))

;; Кэш для хранения данных
(def data-cache (atom (cache/ttl-cache-factory {:ttl 3600})))

(defn get-auth-token [username password auth-url]
  (let [url (str auth-url "/auth/userauth")
        body (json/generate-string {:userName username :password password})
        response (http/post {:url url :body body :content-type :json})]
    response))

(defn add-cache-headers [response]
  (update response :headers assoc
          "Content-Type" "application/json"
          "Cache-Control" "public, max-age=3600"))

;; Основная функция для обработки HTTP-запросов
(defn app [request]
  (let [body-json (-> request :body json/parse-string true)
        username (get body-json "userName")
        password (get body-json "password")
        auth-url (or (System/getenv "AUTH_SERVICE_URL") "http://your-default-auth-service-url")]
    (log/info (str "Received request with JSON body: " body-json))
    (log/info (str "Extracted username: " username " and password: " password))
    (log/info (str "Auth service URL: " auth-url))
    (if (and username password)
      (let [token-response (get-auth-token username password auth-url)]
        (if (= 200 (:status token-response))
          (let [auth-response-body (json/parse-string (:body token-response) true)
                cached-data {:clientIdentifier (:clientIdentifier auth-response-body)
                             :userName (:userName auth-response-body)
                             :userBranch (:userBranch auth-response-body)
                             :systemDate (:systemDate auth-response-body)}]
            (swap! data-cache assoc username cached-data)
            (add-cache-headers {:status 200
                                :headers {"Content-Type" "application/json"}
                                :body (:body token-response)}))
          (add-cache-headers {:status 400
                              :body "Authentication failed. Invalid username or password."})))
      (add-cache-headers {:status 400
                          :body "Bad Request. Provide both username and password."}))))


;; Функция для старта веб-сервера
(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (jetty/run-jetty (wrap-params app) {:port port})))

;; Запуск веб-сервера при старте приложения
(-main)
