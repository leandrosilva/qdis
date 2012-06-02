(ns qdis.web.handler
  (:use [clojure.data.json :only (json-str)])
  (:use compojure.core)
  (:use ring.middleware.stacktrace)
  (:require qdis.engine.queue)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defroutes main-routes
  (GET "/ping" [] "pong")

  (GET "/queues" []
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json-str {"queues" (qdis.engine.queue/queues)})})

  (POST "/queue/:queue/enqueue" [queue item]
    (let [item-uuid (qdis.engine.queue/enqueue queue item)]
      {:status 201
       :headers {"Content-Type" "application/json"
                 "Location" (str "/queue/" queue "/" item-uuid "/status")}
       :body (json-str {"queue" queue
                        "item-uuid" item-uuid})}))

  (GET "/queue/:queue/dequeue" [queue]
    (let [result (qdis.engine.queue/dequeue queue)]
      (if (= result :queue-not-found-or-is-empty)
        {:status 404
         :headers {"Content-Type" "application/json"}
         :body (json-str {"queue" queue
                          "message" "Queue not found or is empty"})}
         
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json-str {"queue" queue
                          "item-uuid" (:item-uuid result)
                          "item" (:item result)})})))

  (GET "/queue/:queue/:item-uuid/status" [queue item-uuid]
    (let [result (qdis.engine.queue/get-item-status item-uuid)]
      (if (= result :item-uuid-not-found)
        {:status 404
         :headers {"Content-Type" "application/json"}
         :body (json-str {"queue" queue
                          "item-uuid" item-uuid
                          "message" "Item-UUID not found"})}
         
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json-str {"queue" queue
                          "item-uuid" item-uuid
                          "status" result})})))

  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-stacktrace)))
