(ns clj-djl.training.activation-test
  (:require [clojure.test :refer :all]
            [clj-djl.ndarray :as nd]
            [clj-djl.model :as model]
            [clj-djl.training :as train]
            [clj-djl.training.loss :as loss]
            [clj-djl.nn :as nn])
  (:import [ai.djl.training.initializer Initializer]))

(def config (-> (loss/l2-loss) (train/new-default-training-config) (train/opt-initializer Initializer/ONES)))

(deftest relu-test
  (try (let [model (model/new-instance "model")]
         (model/set-block model (nn/relu-block))
         (try (let [trainer (train/new-trainer model config)]
                (train/initialize trainer [(nd/new-shape [3])])
                (let [manager (train/get-manager trainer)
                      data (nd/create manager (float-array [-1 0 2]))
                      expected (nd/create manager (float-array [0 0 2]))
                      result (nd/singleton-or-throw (train/forward trainer [data]))]
                  (is (= expected (nn/relu data)))
                  (is (= expected result))))))))

(deftest sigmoid-test
  (try (let [model (model/new-instance "model")]
         (model/set-block model (nn/sigmoid-block))
         (try (let [trainer (train/new-trainer model config)]
                (let [manager (train/get-manager trainer)
                      data (nd/create manager (float-array [0]))
                      expected (nd/create manager (float-array [0.5]))
                      result (nd/singleton-or-throw (train/forward trainer [data]))]
                  (is (= expected (nn/sigmoid data)))
                  (is (= expected result))))))))

(deftest tanh-test
  (try (let [model (model/new-instance "model")]
         (model/set-block model (nn/tanh-block))
         (let [trainer (train/new-trainer model config)]
           (let [manager (train/get-manager trainer)
                 data (nd/create manager (float-array [0]))
                 expected (nd/create manager (float-array [0]))
                 result (nd/singleton-or-throw (train/forward trainer [data]))]
             (is (= expected (nn/tanh data)))
             (is (= expected result)))))))

(deftest softrelu-test
  (with-open [model (model/new-model {:name "model"
                                      :block (nn/softplus-block)})
              trainer (train/new-trainer model config)
              manager (train/get-manager trainer)]
    (let [data (nd/create manager [0. 0. 2.])
          expected (nd/create manager [0.6931 0.6931 2.1269])]
      (nd/all-close (nn/softplus data) expected 0.0001 0.0001 true))))
