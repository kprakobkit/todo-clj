(ns todo-clj.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

(defonce app-state (atom {:todos []}))

(defn trace [content]
  (println content))

(defn handler [response cursor]
  (om/update! cursor :todos response))

(defn todo [todo]
  (dom/li #js {:key (:id todo)} (:title todo)))

(defn add-todo [app owner]
  (let [title (.-value (om/get-node owner "new-todo"))]    
    (POST "/todos" {:handler #(om/transact! app :todos (fn [todos] (conj todos %)))
                    :error-handler trace
                    :params {:title title}
                    :format :json
                    :response-format :json
                    :keywords? true})))

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
        (dom/h1 nil "Todo")
        (dom/div nil
          (dom/input #js {:ref "new-todo" :type "text"} nil)
          (dom/button #js {:onClick #(add-todo app owner)} "Submit"))
        (dom/ul nil (map todo (get app :todos)))))))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})
