(ns clj-auth-cache.auth-proxy
  (:require [clojure.core.cache :as cache]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]))

;; Example cache with TTL (time-to-live) configured via environment variable
(def token-cache (atom (cache/ttl-cache-factory {:ttl (or (Integer/getInteger "CACHE_TTL_SECONDS" 3600))})))

;; Function to obtain an authorization token
(defn get-auth-token [username password auth-url]
  (let [url (str auth-url "/auth/userauth")
        body (str "{\"userName\":\"" username "\",\"password\":\"" password "\"}")
        response (http/post {:url url :body body :content-type :json})]
    (:authToken (-> response :body (json/read-str true)))))

;; Function to get a cached authorization token
(defn get-cached-auth-token [username password auth-url]
  (or
    (cache/lookup token-cache username)
    (let [token (get-auth-token username password auth-url)]
      (swap! token-cache cache/assoc username token)
      token)))

;; Function to get information by token
(defn get-info-by-token [token auth-url]
  (let [url (str auth-url "/auth/getInfoByToken/" token)
        response (http/get {:url url :headers {"Authorization" (str "Bearer " token)}})]
    (:body response)))

;; Main function
(defn -main [& args]
  (let [auth-url (or (System/getenv "AUTH_SERVICE_URL") "http://default-auth-service-url")]
    (log/info (str "Your Clojure Auth Proxy is running with auth service at: " auth-url))))

;; Function to check and update the token after 10 hours
(defn check-and-update-token [username password auth-url]
  (let [current-token (get-cached-auth-token username password auth-url)
        current-time (System/currentTimeMillis)
        ten-hours-millis (* 10 60 60 1000)]
    (if (> (- current-time (:timestamp @current-token)) ten-hours-millis)
      (do
        (println "Token is older than 10 hours. Updating...")
        (swap! token-cache #(cache/dissoc % username))
        (get-cached-auth-token username password auth-url))
      current-token)))
