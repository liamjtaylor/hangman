;TODO: try out the clj-refactor library
;TODO: which things should be made private and which functions?

(ns backend.hangman
  (:require [clojure.string :as str]))

;TODO: is it possible to avoid all these atoms?
;TODO: when this is concurrent need a data structure per each session
;see how to use promises and such things
(def secret-word (atom ""))
(def masked-word (atom []))
(def dictionary-file "/usr/share/dict/british")
(def all-words (str/split-lines (slurp dictionary-file)))
;; Generate a random string which has to be guessed by different users

(def all-chars (map char (range (int \a) (inc (int \z)))))

;TODO: order matters so be careful to leave things as they should be
(defn pick-random-element
  "Pick a random element from a collection"
  [coll]
  (let [size (count coll)
        index (Math/round (* (Math/random) (dec size)))]
    (nth coll index)))

(defn random-char
  []
  (pick-random-element all-chars))

(defn gen-string
  "Generate a random string"
  [dictionary n]
  (pick-random-element (filter #(= (count %) n) dictionary)))

(defn guess-word
  [word]
  (= word @secret-word))

(defn valid-char
  [char]
  (contains? (set all-chars) char))

(defn initialize-struct
  [word]
  (for [i word]
    ;TODO: can be made more readable?
    {:char i :visible (not (valid-char i))}))

(defn set-secret
  [size]
  (let [word (gen-string all-words size)
        secret-struct (initialize-struct word)]
;TODO: the two operations together are not atomic anymore, need to use
;refs for this purpose now, or maybe some other structure
    (reset! secret-word word)
    (reset! masked-word secret-struct)))

(defn secret-string
  "Join the secret string structure marking hidden chars as _"
  [secret]
  (str/join (map #(if (:visible %) (:char %) \_) secret)))


(defn lowercase-char
  [char]
  ;TODO: this seems quite hacky can it be improved?
  (nth (seq (char-array (.toLowerCase (str char)))) 0))


;TODO: is there a way to print out the current variables in the given function?
(defn filter-char
  [letter el]
  (let [to-find (lowercase-char letter)]
    (if (= (lowercase-char (:char el)) to-find)
      ;TODO: is there a better way to modify this structure inline?
      {:char (:char el) :visible true}
      el)))

(defn reveal-letter
  "Return another secret structure where the revealed chars are marked now as visible"
  [secret letter]
  (map (partial filter-char letter) secret))


(def seen-letters (atom #{}))

(defn found?
  [letter struct]
  (let [founds (filter #(and (= letter (:char %)) (false? (:visible %))) struct)]
    (not (empty? founds))))

(defn move
  "Do one move and, return True if the letter was found or False otherwise"
  [letter]
  (if (found? letter @masked-word)
    (let [newstruct (reveal-letter @masked-word letter)]
      (swap! seen-letters #(conj % letter))
      (reset! masked-word newstruct)
      (secret-string @masked-word)
      true))
  false)


(defn game-over
  [secret-struct]
  (every? :visible secret-struct))
