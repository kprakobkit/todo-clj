(ns todo-clj.actions
  (:require [ajax.core :refer [GET POST DELETE PATCH]]
            [om.core :as om :include-macros true]))

(defn trace [content]
  (println content))

(defn handler [response cursor]
  (om/update! cursor :todos response))

(defn get-todos [app]
  (GET "/todos" {:handler #(handler % app)
                 :keywords? true
                 :response-format :json}))

(defn remove-todo [todo handler] 
  (let [url (:url todo)]
    (DELETE url {:handler handler
                 :error-handler trace
                 :format :json})))

(defn update-todo [todo updates handler]
  (PATCH (:url todo) {:handler handler
                      :error-handler trace
                      :params updates
                      :format :json
                      :response-format :json
                      :keywords? true}))

(defn add-todo [cursor owner]
  (let [title (om/get-state owner :title)]
    (POST "/todos" {:handler (fn [res]
                               (om/transact! cursor :todos (fn [todos] (conj todos res)))
                               (om/set-state! owner :title ""))
                    :error-handler trace
                    :params {:title title}
                    :format :json
                    :response-format :json
                    :keywords? true})))
