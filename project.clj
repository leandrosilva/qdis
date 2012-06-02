(defproject qdis "0.0.1-SNAPSHOT"
  :description "Qdis is a message-oriented middleware with a Web API and back-ended by Redis"
  :dependencies
    [[org.clojure/clojure "1.4.0"]
     [org.clojure/data.json "0.1.2"]
     [commons-pool/commons-pool "1.6"]
     [redis.clients/jedis "2.1.0"]
     [ring/ring-jetty-adapter "1.1.0"]
     [compojure "1.1.0"]]
  :dev-dependencies
    [[ring/ring-devel "1.1.0"]]
  :plugins
    [[lein-ring "0.7.1"]]
  :ring {:handler qdis.web.handler/app})
  