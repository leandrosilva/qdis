(ns qdis.web
  (:use compojure.core)
  (:use ring.middleware.reload)
  (:use ring.middleware.stacktrace)
  (:use qdis.queue)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defroutes main-routes
  (GET "/ping" [] "pong")
  
  (POST "/:queue/enqueue" [queue payload]
    (let [result (qdis.queue/put-in queue payload)]
      {:status 200
       :headers {"Content-Type" "application/json"
                 "Location" (str "/" queue "/:item-uuid/status")}
       :body (str "{\"queue\":\"" queue "\", \"item-uuid\":\"000\"}")}))
    
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-reload '[qdis.web]) ; shoulb be use only in development mode
      (wrap-stacktrace)))
