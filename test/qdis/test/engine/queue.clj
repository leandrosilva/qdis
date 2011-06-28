(ns qdis.test.engine.queue
  (:require qdis.engine.queue)
  (:require qdis.engine.jedis)
  (:use [qdis.config] :reload)
  (:use [clojure.test]))

(deftest life-cycle-test
  (qdis.engine.jedis/initialize-pool {:host "127.0.0.1" :port 6379})

  ; setup
  (qdis.engine.jedis/with-jedis
    (qdis.engine.jedis/-select 1)
    (qdis.engine.jedis/-flushdb))

  ;scenarious
  
  (testing "there's no queues at this point"
    (is [] (qdis.engine.queue/queues)))

  (testing "enqueuing itens"
    (is (= "qdis:queue:padoca:uuid:1"
           (qdis.engine.queue/enqueue "padoca" "panguan1")))
    (is (= "qdis:queue:padoca:uuid:2"
           (qdis.engine.queue/enqueue "padoca" "panguan2")))
    (is (= "qdis:queue:padoca:uuid:3"
           (qdis.engine.queue/enqueue "padoca" "panguan3"))))

  (testing "dequeuing itens"
    (is (= {:item-uuid "qdis:queue:padoca:uuid:1", :item "panguan1"}
           (qdis.engine.queue/dequeue "padoca")))
    (is (= {:item-uuid "qdis:queue:padoca:uuid:2", :item "panguan2"}
           (qdis.engine.queue/dequeue "padoca")))
    (is (= {:item-uuid "qdis:queue:padoca:uuid:3", :item "panguan3"}
           (qdis.engine.queue/dequeue "padoca"))))

  (testing "there's one queue at this point"
    (is ["qdis:queue:padoca"] (qdis.engine.queue/queues)))
  
  (testing "enqueuing itens in other queue"
    (is (= "qdis:queue:pastelaria:uuid:4"
           (qdis.engine.queue/enqueue "pastelaria" "tosquito1")))
    (is (= "qdis:queue:pastelaria:uuid:5"
           (qdis.engine.queue/enqueue "pastelaria" "tosquito2"))))

  (testing "dequeuing itens from other queue"
    (is (= {:item-uuid "qdis:queue:pastelaria:uuid:4", :item "tosquito1"}
           (qdis.engine.queue/dequeue "pastelaria")))
    (is (= {:item-uuid "qdis:queue:pastelaria:uuid:5", :item "tosquito2"}
           (qdis.engine.queue/dequeue "pastelaria"))))

  (testing "there's two queues at this point"
    (is ["qdis:queue:padoca" "qdis:queue:pastelaria"] (qdis.engine.queue/queues)))

  ; teardown
  (qdis.engine.jedis/with-jedis
    (qdis.engine.jedis/-flushdb))

  (qdis.engine.jedis/finalize-pool))