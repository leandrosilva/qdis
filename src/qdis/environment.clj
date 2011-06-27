(ns qdis.environment)

(defonce ^{:private true} *environment* (ref nil))

(defn setup [env]
  (dosync
    (ref-set *environment* env))
  @*environment*)

(defn development? []
  (= @*environment* "development"))

(defn test? []
  (= @*environment* "test"))

(defn ci? []
  (= @*environment* "ci"))

(defn production? []
  (= @*environment* "production"))
