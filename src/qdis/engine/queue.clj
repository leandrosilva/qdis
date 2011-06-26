(ns qdis.engine.queue
  (:require qdis.engine.jedis)
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date))

;; settings

(def ^{:private true} queue-set     "qdis:queue-set")
(def ^{:private true} queue-uuid    "qdis:uuid")
(def ^{:private true} queue-history "qdis:queue-history")

;; private api

(defn- tag-for
  ;; tag for queue => qdis:queue:blah
  ([queue] (str "qdis:queue:" queue))
  ;; tag for item-uuid => qdis:queue:{foo}:uuid:{bar}
  ([queue uuid] (str queue ":uuid:" uuid)))

(defn- status-for
  ;; tag for item's status => qdis:queue:{foo}:uuid:{bar}:status:{dig}
  ([item-uuid status] (str item-uuid ":status:" status))
  ;; tag for current item's status => qdis:queue:{foo}:uuid:{bar}:status
  ([item-uuid] (str item-uuid ":status")))

(defn- right-now []
  (let [formatter (SimpleDateFormat. "MM/dd/yyyy hh:mm:ss")]
    (.format formatter (Date.))))

;; public api

(defn enqueue [queue item]
  (qdis.engine.jedis/with-jedis
    (let [queue-name (tag-for queue)]
      ;; create the queue (if it doesn't exists)
      (qdis.engine.jedis/-sadd queue-set queue-name)
      ;; get a uuid to this new item
      (let [item-uuid (tag-for queue-name (qdis.engine.jedis/-incr queue-uuid))]
        ;; bind this uuid to item's value
        (qdis.engine.jedis/-set item-uuid item)
        ;; bind a status to item
        (qdis.engine.jedis/-set (status-for item-uuid) "enqueued")
        (qdis.engine.jedis/-set (status-for item-uuid "enqueued") (right-now))
        ;; and finally push item's uuid to queue
        (qdis.engine.jedis/-lpush queue-name item-uuid)
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
                         ;; del item
                         (qdis.engine.jedis/-del item-uuid)
                         ;; bind a status to it
                         (qdis.engine.jedis/-set (status-for item-uuid) "dequeued")
                         (qdis.engine.jedis/-set (status-for item-uuid "dequeued") (right-now))
                         ;; and finally push item in history queue
                         (qdis.engine.jedis/-lpush queue-history item-uuid)
                         ;; result
                         {:item-uuid item-uuid :item item}))))]
      result)))
        
;; tests

(defn run-tests []
  (qdis.engine.jedis/initialize-pool {:host "127.0.0.1" :port 6379})

  (println "\n::: running test functions :::\n")

  (println "TEST 1   (enqueue 'padoca' 'panguan') =" (enqueue "padoca" "panguan"))
  (println "TEST 2   (dequeue 'padocax') ="          (dequeue "padocax"))
  (println "TEST 3   (dequeue 'padoca') ="           (dequeue "padoca"))

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
