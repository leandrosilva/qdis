(ns qdis.queue
  (:use qdis.jedis)
  (:import [redis.clients.jedis Jedis]))

;; settings

(def ^{:private true} *queue-set*  "qdis:queueset")
(def ^{:private true} *queue-uuid* "qdis:uuid")

(def ^{:private true} *qdis-tag*  "qdis:")
(def ^{:private true} *queue-tag* "queue:")
(def ^{:private true} *uuid-tag*  ":uuid:")

(defn ^{:private true} which-queue-name-for [queue]
  (str *qdis-tag* *queue-tag* queue))

(defn ^{:private true} which-item-uuid-for [queue-name uuid]
  (str queue-name *uuid-tag* uuid))
  
;; api

(defn enqueue [queue item]
  (let [jedis (qdis.jedis/connect)]
    (let [queue-name (which-queue-name-for queue)]
      ;; create the queue (if it doesn't exists)
      (.sadd jedis *queue-set* queue-name)
      ;; get a uuid to received item
      (let [item-uuid (which-item-uuid-for queue-name (.incr jedis *queue-uuid*))]
        ;; bind this uuid to item's value
        (.set jedis item-uuid item)
        ;; and finally push item's uuid to queue
        (.lpush jedis queue-name item-uuid)
        
        (qdis.jedis/disconnect jedis)
        item-uuid))))

(defn dequeue [queue]
  (let [jedis (qdis.jedis/connect)]
    (let [result (let [queue-name (which-queue-name-for queue)]
                   ;; get item's uuid from queue
                   (let [item-uuid (.rpop jedis queue-name)]
                     (if (nil? item-uuid)
                       ;; being nil, it means that this queue doesn't exists or is empty
                       :queue-not-found-or-is-empty
                       ;; or since queue exists, get and del the item
                       (let [item (.get jedis item-uuid)]
                         (.del jedis (into-array [item-uuid]))
                         {:item-uuid item-uuid :item item}))))]
        
        (qdis.jedis/disconnect jedis)
        result)))
        
;; tests

(defn run-tests []
  (qdis.jedis/initialize-connection-pool! {:host "127.0.0.1" :port 6379})
    
  (println "\n::: running test functions :::\n")

  (println "TEST 1   (enqueue 'padoca' 'panguan') =" (enqueue  "padoca" "panguan"))
  (println "TEST 2   (dequeue 'padocax') ="          (dequeue "padocax"))
  (println "TEST 3   (dequeue 'padoca') ="           (dequeue "padoca"))

  (println)

  (println "TEST 4.1 (enqueue 'padoca' 'panguan1') =" (enqueue  "padoca" "panguan1"))
  (println "TEST 4.2 (enqueue 'padoca' 'panguan2') =" (enqueue  "padoca" "panguan2"))
  (println "TEST 4.3 (enqueue 'padoca' 'panguan3') =" (enqueue  "padoca" "panguan3"))
  (println "TEST 4.4 (dequeue 'padoca') ="            (dequeue "padoca"))
  (println "TEST 4.5 (dequeue 'padoca') ="            (dequeue "padoca"))
  (println "TEST 4.6 (dequeue 'padoca') ="            (dequeue "padoca"))

  (println)

  (qdis.jedis/finalize-connection-pool!)
  :ok)
