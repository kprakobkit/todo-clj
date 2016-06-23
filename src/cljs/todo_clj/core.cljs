(ns todo-clj.core
  (:require [om.core :as om :include-macros true]
            [todo-clj.app :as app]))

(enable-console-print!)

(defonce app-state (atom {:todos []}))

(om/root
 app/app
 app-state
 {:target (js/document.getElementById "app")})
