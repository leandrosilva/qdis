(ns qdis.config)

(defonce ^{:private true} *info* (ref nil))

(defn setup [env]
  (dosync
    (ref-set *info* (load-file (str "config/" env ".clj"))))
  @*info*)

(defn info
  ([] @*info*)
  ([key] (key @*info*)))
