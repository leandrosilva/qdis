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
                "- criar um modulo para encapsular as configuracoes\n"
                "- trabalhar o handler para ficar REST-like\n"
                "- trabalhar o wrap-reload so em dev mode\n")))

(defn before-boot []
  (todo-list)
  (qdis.jedis/initialize-connection-pool!))

(defn after-boot []
  (qdis.jedis/finalize-connection-pool!))

;; boot the server by environment
(defn boot-with [env]
  (let [config (load-file (str "config/" env ".clj"))]
    (before-boot)
    (run-jetty #'qdis.web/app (:server config))
    (after-boot)))

;; server entry point
(defn -run [& args]
  (with-command-line args
      (str "Qdis server usage:\n"
           "  $ ./bin/run --env ENV\n")
      [[env "Environment setting (development|ci|production)" "development"]
       remaining]
       
    (println "Starting server in" env "mode")
    (boot-with env)))

;; runs the server
(apply -run *command-line-args*)
