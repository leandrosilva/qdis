(ns qdis.server
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use ring.adapter.jetty)
  (:use qdis.web))

(defn todo-list []
  (println (str "\nTODO List:\n"
                "- refatorar o modulo jedis para redis\n"
                "- encapsular as chamadas que fazem referencia ao jedis\n"
                "- encapsular toda a api do jedis (as funcoes que eu uso, claro)\n"
                "- criar um modulo para encapsular as configuracoes")))

(defn before-boot []
  (todo-list)
  (qdis.jedis/initialize-connection-pool!))

(defn after-boot []
  (qdis.jedis/finalize-connection-pool!))

;; boot the server by environment and configuration
(defn boot-with [env config-file-path]
  (let [config (load-file config-file-path)]
    (before-boot)
    (run-jetty #'qdis.web/app (:server config))
    (after-boot)))

;; server entry point
(defn -run [& args]
  (with-command-line args
      (str "Qdis server usage:\n"
           "  $ ./bin/run --env ENV --config FILE\n")
           
      [[env    "Environment (development|ci|production)"
               "development"]
               
       [config "Configuration file."
               "config/development.clj"]
               
       remaining]
       
    (println "Starting server in" env "mode, using" config "settings")
    (boot-with env config)))

;; runs the server
(apply -run *command-line-args*)
