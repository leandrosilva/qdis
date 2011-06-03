(ns qdis.jedis
  (:import [redis.clients.jedis Jedis JedisPool]))

;; use a jedis connection pool to be thread safe
(def ^{:private true} *jedis-pool* (ref nil))

(defn initialize-connection-pool [redis-config]
  (dosync
    (ref-set *jedis-pool* (JedisPool. (:host redis-config) (:port redis-config)))))

(defn finalize-connection-pool []
  (.destroy @*jedis-pool*))

(defn connect []
  (let [jedis (.getResource @*jedis-pool*)]
    (.select jedis 0)
    jedis))

(defn disconnect [jedis]
  (.returnResource @*jedis-pool* jedis))

;; redis-like api

(def jedis)

(defmacro with-jedis [& exprs]
  `(do
     (binding [jedis (connect)]
       (let [result# ~@exprs]
         (disconnect jedis)
         result#))))

(defn -set
  ([jedis* key value] (.set jedis* key value))
  ([key value] (-set jedis key value)))

(defn -get
  ([jedis* key] (.get jedis* key))
  ([key] (-get jedis key)))

(defn -del
  ([jedis* key] (.del jedis* (into-array [key])))
  ([key] (-del jedis key)))

(defn -sadd
  ([jedis* set-key value] (.sadd jedis* set-key value))
  ([set-key value] (.sadd jedis set-key value)))

(defn -incr
  ([jedis* key] (.incr jedis* key))
  ([key] (.incr jedis key)))

(defn -lpush
  ([jedis* list-key value] (.lpush jedis* list-key value))
  ([list-key value] (.lpush jedis list-key value)))

(defn -rpop
  ([jedis* list-key] (.rpop jedis* list-key))
  ([list-key] (.rpop jedis list-key)))
