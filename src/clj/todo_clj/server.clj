(ns todo-clj.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [todo-clj.models.todo :as todos])
  (:gen-class))

(defn res-created [todo]
  {:status 200
   :body todo})

(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (resources "/")
  (GET "/todos" _
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (todos/all)})
  (POST "/todos" {body :body}
    (-> body
        todos/create
        res-created)))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-jetty http-handler {:port port :join? false})))
