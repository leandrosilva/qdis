(ns qdis.core
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use qdis.environment)
  (:use qdis.engine.jedis)
  (:use qdis.web.server))

(defn- todo-list []
  (println (str "\nTODO List:\n"
                "- criar um modulo para encapsular as configuracoes\n"
                "- trabalhar o handler para ficar REST-like\n"
                "- trabalhar o wrap-reload so em dev mode\n")))

;; config info

(defonce ^{:private true} *config-info* (ref nil))

(defn config-info
  ([] @*config-info*)
  ([key] (key @*config-info*)))

;; boot phase

(defn- setup-config-info [env]
  (dosync
    (ref-set *config-info* (load-file (str "config/" env ".clj"))))
  @*config-info*)

(defn- print-runtime-info [config]
  (println "\nRuntime info:\n"
           "- environment: [ development:" (qdis.environment/development?)
                          ", ci:"          (qdis.environment/ci?)
                          ", production:"  (qdis.environment/production?) "]\n"
           "- config: [ server:" (:server config)
                     ", redis:"  (:redis config) "]")
  config)

(defn- before-run [config]
  (todo-list)
  (qdis.engine.jedis/initialize-pool (:redis config))
  config)

(defn- before-shutdown [config]
  (qdis.engine.jedis/finalize-pool)
  config)

(defn- run [env]
  (-> (qdis.environment/setup env)
      (setup-config-info)
      (print-runtime-info)
      (before-run)
      (qdis.web.server/start)
      (before-shutdown)))

;; server entry point
(defn -main [& args]
  (with-command-line args
      (str "Qdis server usage:\n"
           "  $ ./bin/run --env ENV\n")
      [[env "Environment setting (development|ci|production)" "development"]
       remaining]
       
    (println "Running server in" env "mode")
    (run env)))

;; boot the server
(apply -main *command-line-args*)
