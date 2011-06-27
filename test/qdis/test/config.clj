(ns qdis.test.config
  (:use [qdis.config] :reload)
  (:use [clojure.test]))

(def a-config-info-for-test {:server {:port 3001}
                             :redis  {:host "127.0.0.1"
                                      :port 6379}})

(deftest life-cycle-test
  (testing "setup config by environment"
    (is (= a-config-info-for-test (qdis.config/setup "test"))))
    
  (testing "getting info"
    (let [info (qdis.config/info)]
      (is (= {:port 3001} (:server info)))
      (is (= {:host "127.0.0.1" :port 6379} (:redis info)))))
      
  (testing "which environment is it"
    (is (= "test" (qdis.config/which))))
    
  (testing "if is a config for a given environment"
    (is (= false (qdis.config/is-development?)))
    (is (= true (qdis.config/is-test?)))
    (is (= false (qdis.config/is-integration?)))
    (is (= false (qdis.config/is-production?)))))
