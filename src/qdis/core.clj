(ns qdis.core
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:require qdis.config)
  (:require qdis.engine.jedis)
  (:require qdis.web.server))

(defn- print-todo-list []
  (println (str "\nTODO List:\n"
                "- implementar os testes usando a lib padrao de testes\n"
                "- estudar sobre o uso explicito de namespaces\n"
                "- trabalhar o handler para ficar REST-like\n"
                "- trabalhar o wrap-reload so em dev mode\n")))

;; running phase

(defn- before-run [config-info]
  (print-todo-list)
  (qdis.engine.jedis/initialize-pool (:redis config-info))
  config-info)

(defn- before-shutdown [config-info]
  (qdis.engine.jedis/finalize-pool)
  config-info)

(defn- run [env]
  (-> (qdis.config/setup env)
      (before-run)
      (qdis.web.server/start)
      (before-shutdown)))

;; server entry point
(defn -main [& args]
  (with-command-line args
    (str "Qdis server usage:\n"
         "  $ ./bin/run --env ENV\n")
    [[env
      "Environment setting (development|test|integration|production)"
      "development"]
     remaining]
       
    (println "Running server in" env "mode")
    (run env)))
