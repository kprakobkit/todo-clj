(ns todo-clj.app
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [om.dom :as dom :include-macros true]
              [om.core :as om :include-macros true]
              [cljs.core.async :refer [put! chan <! alts!]]
              [todo-clj.actions :refer [get-todos add-todo]]
              [todo-clj.todo :refer [todo-view]]))

(defn handle-key-press [e cursor owner]
  (let [code (.-charCode e)]
    (if (= code 13)
      (add-todo cursor owner))))

(defn handle-change [e owner]
  (let [title (.. e -target -value)]
    (om/set-state! owner :title title)))

(defn remove-todo! [app todo]
  (om/transact! app :todos
                (fn [todos] (vec (remove #(=  todo %) todos)))))

(defn add-todo! [app todo]
  (om/transact! app :todos
                (fn [todos]
                  (let [idx (.indexOf (map #(:url %) todos) (:url todo))]
                    (assoc todos idx todo)))))

(defn app [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:delete (chan)
       :update (chan)
       :title ""})
    om/IWillMount
    (will-mount [_]
      (let [delete (om/get-state owner :delete)
            update (om/get-state owner :update)]
        (get-todos app)
        (go (loop []
              (let [[todo chan] (alts! [delete update])]
                (condp = chan
                  delete (remove-todo! app todo)
                  update (add-todo! app todo))
                (recur))))))
    om/IRenderState
    (render-state [this {:keys [delete title update]}]
      (dom/div nil
        (dom/h1 nil "My Todo")
        (dom/input #js {:ref "new-todo"
                        :type "text"
                        :placeholder "What needs to be done?"
                        :value title
                        :onChange #(handle-change % owner)
                        :onKeyPress #(handle-key-press % app owner)} nil)
        (apply dom/ul nil
          (map
           (fn [todo]
             (om/build todo-view todo
                       {:react-key (:id todo)
                        :init-state {:delete delete
                                     :update update}}))
           (:todos app)))))))
