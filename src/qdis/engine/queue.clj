(ns qdis.engine.queue
  (:require qdis.engine.jedis)
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date))

;; settings

(def ^{:private true} queue-names "qdis:queue-names")
(def ^{:private true} queue-uuid  "qdis:uuid")
(def ^{:private true} queue-log   "qdis:queue-log")

;; private api

(defn- tag-for
  ;; tag for queue => qdis:queue:{blah}
  ([queue] (str "qdis:queue:" queue))
  ;; tag for item-uuid => qdis:queue:{foo}:uuid:{bar}
  ([queue uuid] (str queue ":uuid:" uuid)))

(defn- tag-for-log [item-uuid]
  ;; tag for log and item => qdis:queue:{foo}:uuid:{bar}:log:value
  (str item-uuid ":log:value"))

(defn- status-for [item-uuid]
  ;; tag for current item's status => qdis:queue:{foo}:uuid:{bar}:status
  (str item-uuid ":status"))

(defn- status-for-log [item-uuid status]
  ;; tag for log item's status => qdis:queue:{foo}:uuid:{bar}:log:status:{dig}
  (str item-uuid ":log:status:" status))

(defn- right-now []
  (let [formatter (SimpleDateFormat. "MM/dd/yyyy hh:mm:ss")]
    (.format formatter (Date.))))

;; public api

(defn queues []
  (qdis.engine.jedis/with-jedis
    (qdis.engine.jedis/-smembers queue-names)))

(defn enqueue [queue item]
  (qdis.engine.jedis/with-jedis
    (let [queue-name (tag-for queue)]
      ;; create the queue (if it doesn't exists)
      (qdis.engine.jedis/-sadd queue-names queue-name)
      ;; get a uuid to this new item
      (let [item-uuid (tag-for queue-name (qdis.engine.jedis/-incr queue-uuid))]
        ;; bind this uuid to the item's value
        (qdis.engine.jedis/-set item-uuid item)
        ;; bind a status to the item
        (qdis.engine.jedis/-set (status-for item-uuid) "enqueued")
        ;; push the item to the queue
        (qdis.engine.jedis/-lpush queue-name item-uuid)
        ;; and finally log it
        (qdis.engine.jedis/-set (tag-for-log item-uuid) item)
        (qdis.engine.jedis/-set (status-for-log item-uuid "enqueued") (right-now))
        ;; result
        item-uuid))))

(defn dequeue [queue]
  (qdis.engine.jedis/with-jedis
    (let [result (let [queue-name (tag-for queue)]
                   ;; get item's uuid from queue
                   (let [item-uuid (qdis.engine.jedis/-rpop queue-name)]
                     (if (nil? item-uuid)
                       ;; being nil, it means that this queue doesn't exists or is empty
                       :queue-not-found-or-is-empty
                       ;; or since queue exists
                       (let [item (qdis.engine.jedis/-get item-uuid)]
                         ;; del the item
                         (qdis.engine.jedis/-del item-uuid)
                         ;; bind a status to it
                         (qdis.engine.jedis/-set (status-for item-uuid) "dequeued")
                         ;; and finally log it
                         (qdis.engine.jedis/-set (status-for-log item-uuid "dequeued") (right-now))
                         (qdis.engine.jedis/-lpush queue-log item-uuid)
                         ;; result
                         {:item-uuid item-uuid :item item}))))]
      result)))
        
;; tests

(defn run-tests []
  (qdis.engine.jedis/initialize-pool {:host "127.0.0.1" :port 6379})

  (println "\n::: running test functions :::\n")

  (println "TEST 1   (queues) ="                      (queues))

  (println "\n---\n")

  (println "TEST 1   (enqueue 'padoca' 'panguan') ="  (enqueue "padoca" "panguan"))
  (println "TEST 2   (dequeue 'padocax') ="           (dequeue "padocax"))
  (println "TEST 3   (dequeue 'padoca') ="            (dequeue "padoca"))

  (println)

  (println "TEST 4.1 (enqueue 'padoca' 'panguan1') =" (enqueue "padoca" "panguan1"))
  (println "TEST 4.2 (enqueue 'padoca' 'panguan2') =" (enqueue "padoca" "panguan2"))
  (println "TEST 4.3 (enqueue 'padoca' 'panguan3') =" (enqueue "padoca" "panguan3"))
  (println "TEST 4.4 (dequeue 'padoca') ="            (dequeue "padoca"))
  (println "TEST 4.5 (dequeue 'padoca') ="            (dequeue "padoca"))
  (println "TEST 4.6 (dequeue 'padoca') ="            (dequeue "padoca"))

  (println)
  
  (qdis.engine.jedis/finalize-pool)
  
  :ok)
