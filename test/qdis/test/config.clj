(ns qdis.test.config
  (:use [qdis.config] :reload)
  (:use [clojure.test]))

(def config-info-for-test {:server {:port 3001}
                           :redis  {:host "127.0.0.1"
                                    :port 6379}})

(deftest can-be-loaded-from-a-file
  (is (= config-info-for-test (qdis.config/setup "test"))))

(deftest can-be-read-by-key
  (testing "server info"
    (is (= {:port 3001} (:server (qdis.config/info)))))
  (testing "redis info"
    (is (= {:host "127.0.0.1" :port 6379} (:redis (qdis.config/info))))))
