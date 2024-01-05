(ns clj-auth-cache.auth-proxy
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [clojure.core.cache :as cache]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [ring.util.response :as response]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes ANY]])

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

(defn fetch-user-data [username]
  ;; Your logic to fetch user data from the cache or make a request if not present
  (or (cache/lookup @data-cache username)
      (let [dummy-data {:clientIdentifier "123" ; replace with actual data fetching logic
                        :userName username
                        :userBranch "some-branch"
                        :systemDate "2024-01-05"}]
        (swap! data-cache assoc username dummy-data)
        dummy-data)))

(def app
  (-> (defroutes
        (ANY "/userauth" {:params params :body body :as request}
          (let [username (get params "userName")
                password (get params "password")
                auth-url (or (System/getenv "AUTH_SERVICE_URL") "http://your-default-auth-service-url")]
            (log/info (str "Received request with raw parameters: " params))
            (log/info (str "Extracted username: " username " and password: " password))
            (log/info (str "Auth service URL: " auth-url))

            (if (and username password)
              (let [token-response (get-auth-token username password auth-url)]
                (if (= 200 (:status token-response))
                  (let [body-json (json/parse-string (:body token-response) true)
                        cached-data (fetch-user-data username)]
                    (add-cache-headers {:status 200
                                        :headers {"Content-Type" "application/json"}
                                        :body (json/generate-string cached-data)}))
                  (add-cache-headers {:status 400
                                      :body "Authentication failed. Invalid username or password."})))
              (add-cache-headers {:status 400
                                  :body "Bad Request. Provide both username and password."}))))
      wrap-params
      wrap-json-body
      wrap-json-response))

(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (jetty/run-jetty app {:port port})))

(-main)
