(ns qdis.web.server
  (:use ring.adapter.jetty)
  (:use qdis.web.handler))

(defn start [config]
  (run-jetty #'qdis.web.handler/app (:server config)))
