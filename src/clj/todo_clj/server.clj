(ns todo-clj.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT PATCH POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [todo-clj.models.todo :as todos]
            [clojure.walk :refer [prewalk prewalk-demo]])
  (:gen-class))

(defn res-created [todo]
  {:status 201
   :body todo
   :headers {"Content-Type" "application/json"
             "Location" (str "/todos/" (:id todo))}})

(defn res-ok [body]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body body})

(defn res-no-content []
  {:status 204})

(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (resources "/")
  (GET "/todos" _
    (-> (todos/all)
        res-ok))
  (GET "/todos/:id" {{id :id} :params}
    (-> id
        todos/find-by-id
        res-ok))
  (POST "/todos" {body :body}
    (-> body
        todos/create
        res-created))
  (PATCH "/todos/:id" {{id :id :as params} :params body :body}
    (-> id
        (#(todos/update-todo % body))
        res-created))
  (DELETE "/todos" _
    (todos/delete-all)
    (res-no-content))
  (DELETE "/todos/:id" {{id :id} :params}
    (todos/delete id)
    (res-no-content)))

(defn build-host-name [{name :server-name port :server-port}]
  (str "http://" name ":" port))

(defn expand-url-body [body host-name]
  (prewalk #(if (map? %)
              (assoc % :url (str host-name (:url %)))
              %) body))

(defn wrap-response-url-body [handler]
  (fn [request]
    (let [response (handler request)
          host-name (build-host-name request)
          body (:body response)
          url (:url body)]
      (assoc response :body (expand-url-body body host-name)))))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-response-url-body
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-jetty http-handler {:port port :join? false})))
