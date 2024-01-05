(defproject clj-auth-cache "0.1.0-SNAPSHOT"
  :description "Clojure Auth Cache Proxy"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [ring/ring-core "1.11.0"]
                 [ring/ring-jetty-adapter "1.11.0"]
                 [clj-http "3.12.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.10.0"]
                 [org.clojure/core.cache "1.0.217"]]
  :main ^:skip-aot clj-auth-cache.auth-proxy          
  :target-path "target/%s"
  :profiles
  {:uberjar {:aot :all
             :omit-source true}})


