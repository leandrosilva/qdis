(ns qdis.queue
  (:use qdis.jedis)
  (:import [redis.clients.jedis Jedis]))

;; queue setting
(def *queue-set*    "QUEUESET")
(def *queue-suffix* ":QUEUE")

;; api

(defn put-in [queue item]
  (let [jedis (qdis.jedis/connect)]
    (let [queue-name (.concat queue *queue-suffix*)]
      ;; create queue
      (.sadd jedis *queue-set* queue-name)
      ;; push item to queue
      (.lpush jedis queue-name item)
      
      (qdis.jedis/disconnect jedis)
      :enqueued)))

(defn get-from [queue]
  (let [jedis (qdis.jedis/connect)]
    (let [result (let [queue-name (.concat queue *queue-suffix*)]
                   ;; get item from queue
                   (let [item (.rpop jedis queue-name)]
                     (if (nil? item)
                       ;; being null, it means that the queue does not exist
                       :queue-not-found
                       ;; or since queue exists, del the item
                       item)))]
        
        (qdis.jedis/disconnect jedis)
        result)))
        
;; tests

(defn run-tests []
  (qdis.jedis/initialize-connection-pool!)
    
  (println "\n::: running test functions :::\n")

  (println "TEST 1   (put-in   'padoca' 'panguan') =" (put-in  "padoca" "panguan"))
  (println "TEST 2   (get-from 'padocax') ="          (get-from "padocax"))
  (println "TEST 3   (get-from 'padoca') ="           (get-from "padoca"))

  (println)

  (println "TEST 4.1 (put-in   'padoca' 'panguan1') =" (put-in  "padoca" "panguan1"))
  (println "TEST 4.2 (put-in   'padoca' 'panguan2') =" (put-in  "padoca" "panguan2"))
  (println "TEST 4.3 (put-in   'padoca' 'panguan3') =" (put-in  "padoca" "panguan3"))
  (println "TEST 4.4 (get-from 'padoca') ="            (get-from "padoca"))
  (println "TEST 4.5 (get-from 'padoca') ="            (get-from "padoca"))
  (println "TEST 4.6 (get-from 'padoca') ="            (get-from "padoca"))

  (println)

  (qdis.jedis/finalize-connection-pool!)
  :ok)
