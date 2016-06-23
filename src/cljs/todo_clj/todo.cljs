(ns todo-clj.todo
  (:require [om.core :as om :include-macros true]
            [todo-clj.actions :refer [remove-todo update-todo]]
            [cljs.core.async :refer [put! chan <! alts!]]
            [om.dom :as dom :include-macros true]))

(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))

(defn save-todo [todo owner]
  (let [title (:title todo)]
    (update-todo todo {:title title}
     #(om/set-state! owner :editing false))))

(defn handle-change [e todo]
  (let [title (.. e -target -value)]
    (om/transact! todo #(assoc % :title title))))

(defn todo-view [todo owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false})
    om/IRenderState
    (render-state [_ {:keys [delete update editing]}]
      (dom/li #js {:key (str (:id todo) (rand)) :className "todo"}
              (dom/span #js {:style (display (not editing))
                             :onDoubleClick #(om/set-state! owner :editing true)}
                        (:title todo))
              (dom/input #js {:ref "edit-todo"
                              :value (:title todo)
                              :autoFocus true
                              :style (display editing)
                              :onBlur #(save-todo todo owner)
                              :onChange #(handle-change % todo)})
              (dom/input #js {:type "checkbox"
                              :checked (:completed todo)
                              :onClick #(update-todo todo {:completed (not (:completed todo))} (fn [updated-todo] (put! update updated-todo)))})
              (dom/button #js {:className "todo__remove"
                               :onClick #(remove-todo todo (fn [_] (put! delete @todo)))} "x")))))
