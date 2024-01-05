(defproject clj-auth-cache "0.1.0-SNAPSHOT"
  :description "Clojure Auth Cache Proxy"
  :dependencies [[org.clojure/clojure "1.10.4"]
                 [ring/ring-core "1.11.0"]
                 [ring/ring-jetty-adapter "1.11.0"]
                 [clj-http "3.12.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [cheshire "5.10.0"]
                 [org.clojure/core.cache "0.14.0"]]
  :main ^:skip-aot clj-auth-cache.auth-proxy
  :target-path "target/%s"
  :profiles
  {:uberjar {:aot :all
             :omit-source true}})
