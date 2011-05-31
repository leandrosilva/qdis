(defproject qdis "0.0.1-SNAPSHOT"
  :description "Qdis is a message-oriented middleware with a Web API and back-ended by Redis"
  :dependencies
    [[org.clojure/clojure "1.2.0"]
     [org.clojure/clojure-contrib "1.2.0"]
     [redis.clients/jedis "1.5.2"]
     [commons-pool/commons-pool "1.4"]
     [compojure "0.6.3"]]
  :dev-dependencies
    [[lein-ring "0.4.0"]]
  :ring {:handler qdis.web/app})