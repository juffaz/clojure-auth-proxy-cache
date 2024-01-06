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
  (let [url (str auth-url "/userauth")
        body (json/generate-string {:userName username :password password})
        response (http/post url {:body body :content-type :json})]
    response))

(defn add-cache-headers [response]
  (update response :headers assoc
          "Content-Type" "application/json
          "Cache-Name" "Clojure"
          "Cache-Control" "public, max-age=3600"))

;; Основная функция для обработки HTTP-запросов
(defn app [request]
  (let [params (:body request)
        username (get params "userName")
        password (get params "password")
        auth-url (or (System/getenv "AUTH_SERVICE_URL") "http://apigw.service.test-consul/gni/auth")]
        (log/info (str "Received request with raw parameters: " params))
        (log/info (str "Extracted username: " username " and password: " password))
        (log/info (str "Auth service URL: " auth-url))
    (if (and username password)
      (let [token-response (get-auth-token username password auth-url)]
        (if (= 200 (:status token-response))
          (let [body-json (json/parse-string (:body token-response) true)
                cached-data {:clientIdentifier (:clientIdentifier body-json)
                             :userName (:userName body-json)
                             :userBranch (:userBranch body-json)
                             :systemDate (:systemDate body-json)}]
            (swap! data-cache assoc username cached-data)
            (add-cache-headers {:status 200
                                :headers {"Content-Type" "application/json"}
                                :body (:body token-response)}))
          (add-cache-headers {:status 400
                              :body "Authentication failed. Invalid username or password."})))
      (add-cache-headers {:status 400
                          :body "Bad Request. Provide both username and password."}))))

(def handler (wrap-json-body app))

;; Функция для старта веб-сервера
(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (jetty/run-jetty #'handler {:port port :join? false})))
