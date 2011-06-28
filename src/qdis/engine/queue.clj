(ns qdis.engine.queue
  (:require qdis.engine.jedis)
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date))

;; settings

(def ^{:private true} global-uuid               "qdis:uuid")
(def ^{:private true} global-queue-names        "qdis:queue-names")
(def ^{:private true} global-queue-log-enqueued "qdis:queue-log:enqueued")
(def ^{:private true} global-queue-log-dequeued "qdis:queue-log:dequeued")

;; private api

(defn- tag-for
  ;; tag for queue => qdis:queue:{queue}
  ([queue] (str "qdis:queue:" queue))
  ;; tag for item-uuid => qdis:queue:{queue}:uuid:{uuid}
  ([queue uuid] (str queue ":uuid:" uuid)))

(defn- tag-for-log [item-uuid]
  ;; tag for log and item => qdis:queue:{queue}:uuid:{uuid}:log:value
  (str item-uuid ":log:value"))

(defn- status-for [item-uuid]
  ;; tag for current item's status => qdis:queue:{queue}:uuid:{uuid}:status
  (str item-uuid ":status"))

(defn- status-for-log [item-uuid status]
  ;; tag for log item's status => qdis:queue:{queue}:uuid:{uuid}:log:status:{status}
  (str item-uuid ":log:status:" status))

(defn- right-now []
  (let [formatter (SimpleDateFormat. "MM/dd/yyyy hh:mm:ss")]
    (.format formatter (Date.))))

;; public api

(defn queues []
  (qdis.engine.jedis/with-jedis
    (qdis.engine.jedis/-smembers global-queue-names)))

(defn enqueue [queue item]
  (qdis.engine.jedis/with-jedis
    (let [queue-name (tag-for queue)]
      ;; create the queue (if it doesn't exists)
      (qdis.engine.jedis/-sadd global-queue-names queue-name)
      ;; get a uuid to this new item
      (let [item-uuid (tag-for queue-name (qdis.engine.jedis/-incr global-uuid))]
        ;; bind this uuid to the item's value
        (qdis.engine.jedis/-set item-uuid item)
        ;; bind a status to the item
        (qdis.engine.jedis/-set (status-for item-uuid) "enqueued")
        ;; push the item to the queue
        (qdis.engine.jedis/-lpush queue-name item-uuid)
        ;; and finally log it
        (qdis.engine.jedis/-set (tag-for-log item-uuid) item)
        (qdis.engine.jedis/-set (status-for-log item-uuid "enqueued") (right-now))
        (qdis.engine.jedis/-lpush global-queue-log-enqueued item-uuid)
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
                         (qdis.engine.jedis/-lpush global-queue-log-dequeued item-uuid)
                         ;; result
                         {:item-uuid item-uuid :item item}))))]
      result)))

(defn get-item-status [item-uuid]
  (qdis.engine.jedis/with-jedis
    (let [status (qdis.engine.jedis/-get (status-for item-uuid))]
      (if (nil? status)
        ;; there's no that item
        :item-uuid-not-found
        ;; but if exists
        status))))
