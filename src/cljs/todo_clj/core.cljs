(ns todo-clj.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET]]))

(enable-console-print!)

(defonce app-state (atom {:text "Hello Chestnut!"}))

(defn handler [response]
  (.log js/console (str response)))

(defn root-component [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (GET "/todos" {:handler handler}))
    om/IRender
    (render [_]
      (dom/div nil (dom/h1 nil (:text app))))))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})
