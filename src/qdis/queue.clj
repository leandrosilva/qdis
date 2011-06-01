(ns qdis.queue
  (:use qdis.jedis)
  (:import [redis.clients.jedis Jedis]))

;; queue setting
(def ^{:private true} *top-level-tag*  "qdis:")
(def ^{:private true} *queue-set*      "qdis:queueset")
(def ^{:private true} *queue-tag*      "queue:")
(def ^{:private true} *queue-uuid-tag* ":uuid:")

(defn ^{:private true} which-queue-name [queue]
  (str *top-level-tag* *queue-tag* queue))

(defn ^{:private true} which-queue-uuid [queue-name]
  (str queue-name *queue-uuid-tag*))

(defn ^{:private true} which-item-uuid [queue-uuid uuid]
  (str queue-uuid uuid))
  
;; api

(defn put-in [queue item]
  (let [jedis (qdis.jedis/connect)]
    (let [queue-name (which-queue-name queue)]
      ;; create the queue (if it doesn't exists)
      (.sadd jedis *queue-set* queue-name)
      ;; get a uuid to received item
      (let [item-uuid (let [queue-uuid (which-queue-uuid queue-name)]
                        (which-item-uuid queue-uuid (.incr jedis queue-uuid)))]
        ;; bind this uuid to item's value
        (.set jedis item-uuid item)
        ;; and finally push item's uuid to queue
        (.lpush jedis queue-name item-uuid)
        
        (qdis.jedis/disconnect jedis)
        item-uuid))))

(defn get-out [queue]
  (let [jedis (qdis.jedis/connect)]
    (let [result (let [queue-name (which-queue-name queue)]
                   ;; get item's uuid from queue
                   (let [item-uuid (.rpop jedis queue-name)]
                     (if (nil? item-uuid)
                       ;; being null, it means that the queue does not exist
                       :queue-not-found
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

  (println "TEST 1   (put-in  'padoca' 'panguan') =" (put-in  "padoca" "panguan"))
  (println "TEST 2   (get-out 'padocax') ="          (get-out "padocax"))
  (println "TEST 3   (get-out 'padoca') ="           (get-out "padoca"))

  (println)

  (println "TEST 4.1 (put-in  'padoca' 'panguan1') =" (put-in  "padoca" "panguan1"))
  (println "TEST 4.2 (put-in  'padoca' 'panguan2') =" (put-in  "padoca" "panguan2"))
  (println "TEST 4.3 (put-in  'padoca' 'panguan3') =" (put-in  "padoca" "panguan3"))
  (println "TEST 4.4 (get-out 'padoca') ="            (get-out "padoca"))
  (println "TEST 4.5 (get-out 'padoca') ="            (get-out "padoca"))
  (println "TEST 4.6 (get-out 'padoca') ="            (get-out "padoca"))

  (println)

  (qdis.jedis/finalize-connection-pool!)
  :ok)
