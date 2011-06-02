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
                "- trabalhar o wrap-reload so em dev mode\n"
                "- conferir os nomes das funcoes e parametros customizados do jedis\n"
                "- refatorar initialize-connection-pool para apenas pool\n"
                "- refatorar para matar a funcao connection-pool\n")))

(defn load-config-for [env]
  (load-file (str "config/" env ".clj")))

(defn before-serving [config]
  (todo-list)
  (qdis.jedis/initialize-connection-pool! (:redis config))
  config)

(defn serving [config]
  (run-jetty #'qdis.web/app (:server config))
  config)

(defn after-serving [config]
  (qdis.jedis/finalize-connection-pool!)
  config)

;; boot the server by environment
(defn boot [env]
  (-> (load-config-for env)
      (before-serving)
      (serving)
      (after-serving)))

;; server entry point
(defn -run [& args]
  (with-command-line args
      (str "Qdis server usage:\n"
           "  $ ./bin/run --env ENV\n")
      [[env "Environment setting (development|ci|production)" "development"]
       remaining]
       
    (println "Starting server in" env "mode")
    (boot env)))

;; runs the server
(apply -run *command-line-args*)
