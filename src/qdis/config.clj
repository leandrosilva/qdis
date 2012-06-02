(ns qdis.config)

(defonce ^{:private true} ^:dynamic *info* (ref nil))
(defonce ^{:private true} ^:dynamic *environment* (ref nil))

(defn setup [env]
  (dosync
    (ref-set *info* (load-file (str "config/" env ".clj")))
    (ref-set *environment* (keyword env)))
  @*info*)

(defn info
  ([] @*info*)
  ([key] (key @*info*)))

(defn which? []
  @*environment*)

(defn is-development? []
  (= @*environment* :development))

(defn is-test? []
  (= @*environment* :test))

(defn is-integration? []
  (= @*environment* :integration))

(defn is-production? []
  (= @*environment* :production))
