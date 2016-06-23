(ns todo-clj.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts!]]
            [todo-clj.actions :refer [remove-todo update-todo get-todos add-todo]]))

(enable-console-print!)

(defonce app-state (atom {:todos []}))


(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))

(defn save-todo [todo owner]
  (let [title (:title todo)]
    (update-todo todo {:title title}
     #(om/set-state! owner :editing false))))

(defn handle-edit-title [e todo]
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
                              :onChange #(handle-edit-title % todo)})
              (dom/input #js {:type "checkbox"
                              :checked (:completed todo)
                              :onClick #(update-todo todo {:completed (not (:completed todo))} (fn [updated-todo] (put! update updated-todo)))})
              (dom/button #js {:className "todo__remove"
                               :onClick #(remove-todo todo (fn [_] (put! delete @todo)))} "x")))))

(defn handle-key-press [e cursor owner]
  (let [code (.-charCode e)]
    (if (= code 13)
      (add-todo cursor owner))))

(defn handle-change [e owner]
  (let [title (.. e -target -value)]
    (om/set-state! owner :title title)))

(defn root-component [app owner]
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
                  delete
                  (om/transact! app :todos
                                (fn [todos] (vec (remove #(=  todo %) todos))))
                  update
                  (om/transact! app :todos
                                (fn [todos]
                                  (let [idx (.indexOf (map #(:url %) todos) (:url todo))]
                                    (assoc todos idx todo)))))
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

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})
