(ns qdis.web.server
  (:use ring.adapter.jetty)
  (:use qdis.web.handler))

(defn start [config-info]
  (run-jetty #'qdis.web.handler/app (:server config-info)))
