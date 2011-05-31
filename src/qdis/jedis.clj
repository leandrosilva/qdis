(ns qdis.jedis
  (:import [redis.clients.jedis Jedis JedisPool]))

;; use a jedis connection pool to be thread safe
(def ^{:private true} *jedis-pool* (ref nil))

(defn initialize-connection-pool! []
  (dosync
    (alter *jedis-pool*
           (fn [x]
             (JedisPool. "127.0.0.1" 6379)))))

(defn connection-pool []
  @*jedis-pool*)

(defn finalize-connection-pool! []
  (.destroy (connection-pool)))

(defn connect []
  (let [jedis (.getResource (connection-pool))]
    (.select jedis 0)
    jedis))

(defn disconnect [jedis]
  (.returnResource (qdis.jedis/connection-pool) jedis))
