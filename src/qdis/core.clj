(ns qdis.core
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use qdis.web.server))

(defn- todo-list []
  (println (str "\nTODO List:\n"
                "- criar um modulo para encapsular as configuracoes\n"
                "- trabalhar o handler para ficar REST-like\n"
                "- trabalhar o wrap-reload so em dev mode\n"
                "- refatorar para matar a funcao connection-pool\n")))

(defn- load-config-for [env]
  (load-file (str "config/" env ".clj")))

(defn- before-run [config]
  (todo-list)
  (qdis.jedis/initialize-pool (:redis config))
  config)

(defn- before-shutdown [config]
  (qdis.jedis/finalize-pool)
  config)

(defn- run [env]
  (-> (load-config-for env)
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

;; runs the server
(apply -main *command-line-args*)
