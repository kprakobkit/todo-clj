(ns todo-clj.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST DELETE]]))

(enable-console-print!)

(defonce app-state (atom {:todos []}))

(defn trace [content]
  (println content))

(defn handler [response cursor]
  (om/update! cursor :todos response))


(defn remove-todo [todo]
  (let [url (:url todo)]
    (DELETE url {:handler trace
                 :error-handler trace
                 :format :json})))

(defn todo-view [todo]
  (reify
    om/IRender
    (render [_]
      (dom/li #js {:key (str (:id todo) (rand)) :className "todo"}
              (:title todo)
              (dom/button #js {:className "todo__remove" :onClick #(remove-todo todo)} "x")))))

(defn add-todo [app owner]
  (let [title (.-value (om/get-node owner "new-todo"))]    
    (POST "/todos" {:handler #(om/transact! app :todos (fn [todos] (conj todos %)))
                    :error-handler trace
                    :params {:title title}
                    :format :json
                    :response-format :json
                    :keywords? true})))

(defn handle-key-press [e cursor owner]
  (let [code (.-charCode e)]
    (if (= code 13)
      (add-todo cursor owner))))

(defn root-component [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (GET "/todos" {:handler #(handler % app)
                     :keywords? true
                     :response-format :json}))
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h1 nil "My Todo")
        (dom/input #js {:ref "new-todo"
                        :type "text"
                        :placeholder "What needs to be done?"
                        :onKeyPress #(handle-key-press % app owner)}
                   nil)
        (apply dom/ul nil
          (map #(om/build todo-view %) (:todos app)))))))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})
