(ns backend.hangman-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [backend.hangman :refer :all]))

(def sample-dictionary
  "abc
sample
something
")


(def sample-secret
  [{:char \x :visible false}
   {:char \y :visible true}
   {:char \z :visible false}])


(deftest hangman-test
  (testing "Guess string"
    (reset! secret-word "abc")
    (true? (guess-word "abc")))

  (testing "Random element"
    ;; (doseq [i (range 10)]
    (pick-random-element ["abc" "bde"]))

  (testing "Mask"
    (is (= (secret-string sample-secret) "_y_")))

  (testing "reveal"
    (is (= (secret-string (reveal-letter sample-secret \x)) "xy_")))

  (testing "Generation"
    (let [sample (gen-string (str/split-lines sample-dictionary) 3)]
      (is (= "abc" sample)))))