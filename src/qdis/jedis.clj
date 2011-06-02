(ns qdis.jedis
  (:import [redis.clients.jedis Jedis JedisPool]))

;; use a jedis connection pool to be thread safe
(def ^{:private true} *jedis-pool* (ref nil))

(defn initialize-connection-pool! [redis-config]
  (dosync
    (ref-set *jedis-pool* (JedisPool. (:host redis-config) (:port redis-config)))))

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

;; redis-like api

(def *jedis*)

(defmacro with-jedis [& exprs]
  `(do
     (binding [*jedis* (connect)]
       (let [result# (do ~@exprs)]
         (disconnect *jedis*)
         result#))))

(defn set- [key value]
  (.set *jedis* key value))

(defn get- [key]
  (.get *jedis* key))

(defn del- [key]
  (.del *jedis* (into-array [key])))

(defn sadd- [set value]
  (.sadd *jedis* set value))

(defn incr- [uuid]
  (.incr *jedis* uuid))

(defn lpush- [list value]
  (.lpush *jedis* list value))

(defn rpop- [list]
  (.rpop *jedis* list))
