(ns qdis.server
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use ring.adapter.jetty)
  (:use qdis.web))

(defn- todo-list []
  (println (str "\nTODO List:\n"
                "- criar um modulo para encapsular as configuracoes\n"
                "- trabalhar o handler para ficar REST-like\n"
                "- trabalhar o wrap-reload so em dev mode\n"
                "- refatorar para matar a funcao connection-pool\n")))

(defn- load-config-for [env]
  (load-file (str "config/" env ".clj")))

(defn- before-handle-http [config]
  (todo-list)
  (qdis.jedis/initialize-pool (:redis config))
  config)

(defn- handle-http [config]
  (run-jetty #'qdis.web/app (:server config))
  config)

(defn- after-handle-http [config]
  (qdis.jedis/finalize-pool)
  config)

(defn- start-http-server [env]
  (-> (load-config-for env)
      (before-handle-http)
      (handle-http)
      (after-handle-http)))

;; server entry point
(defn -main [& args]
  (with-command-line args
      (str "Qdis server usage:\n"
           "  $ ./bin/run --env ENV\n")
      [[env "Environment setting (development|ci|production)" "development"]
       remaining]
       
    (println "Starting server in" env "mode")
    (start-http-server env)))

;; runs the server
(apply -main *command-line-args*)
