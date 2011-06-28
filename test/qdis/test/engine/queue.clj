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
  
  (println "\n::: running test functions [qdis.test.engine.queue] :::\n")

  (println "TEST 1   (queues) ="                      (qdis.engine.queue/queues))

  (println "\n---\n")

  (println "TEST 1   (enqueue 'padoca' 'panguan') ="  (qdis.engine.queue/enqueue "padoca" "panguan"))
  (println "TEST 2   (dequeue 'padocax') ="           (qdis.engine.queue/dequeue "padocax"))
  (println "TEST 3   (dequeue 'padoca') ="            (qdis.engine.queue/dequeue "padoca"))

  (println)

  (println "TEST 4.1 (enqueue 'padoca' 'panguan1') =" (qdis.engine.queue/enqueue "padoca" "panguan1"))
  (println "TEST 4.2 (enqueue 'padoca' 'panguan2') =" (qdis.engine.queue/enqueue "padoca" "panguan2"))
  (println "TEST 4.3 (enqueue 'padoca' 'panguan3') =" (qdis.engine.queue/enqueue "padoca" "panguan3"))
  (println "TEST 4.4 (dequeue 'padoca') ="            (qdis.engine.queue/dequeue "padoca"))
  (println "TEST 4.5 (dequeue 'padoca') ="            (qdis.engine.queue/dequeue "padoca"))
  (println "TEST 4.6 (dequeue 'padoca') ="            (qdis.engine.queue/dequeue "padoca"))

  (println)

  ; teardown
  (qdis.engine.jedis/with-jedis
    (qdis.engine.jedis/-flushdb))
  
  (qdis.engine.jedis/finalize-pool)
  
  (is (= "temporary test" "temporary test")))
