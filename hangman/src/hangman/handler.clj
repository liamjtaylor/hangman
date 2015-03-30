(ns hangman.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [hangman.hangman :as hang]))

(defn move-api
  [letter]
  "Move and return a JSON response to the client"
  ;; (println "Got letter " letter " to change with type " (class letter))
  (hang/move letter)
  {:status 200 :body (hang/secret-string @hang/masked-word)})

(defn initialize-word
  "Initiaiize a random word"
  ([]
   (do
     (hang/set-secret 10)
     {:status 201}))
  ([secret]
   (do
     (dosync
      (ref-set hang/secret-word secret)
      (ref-set hang/masked-word (hang/initialize-struct secret)))
     {:status 201})))

(defroutes app-routes
  (GET "/" [] "No need to restart the server every time")
  (GET "/status" [] {:status 200 :body (hang/secret-string @hang/masked-word)})
  (POST "/initialize" [] (initialize-word))
  (POST "/move" params  (move-api (nth (:letter (:params params)) 0)))
  (route/not-found "Not found"))

(def app
  (wrap-defaults app-routes api-defaults))
