(ns todo-clj.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET]]))

(enable-console-print!)

(defonce app-state (atom {:todos []}))

(defn handler [response cursor]
  (om/update! cursor :todos response))

(defn todo [todo]
  (dom/li #js {:key (:id todo)} (:title todo)))

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
        (dom/ul nil (map todo (get app :todos)))))))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})
