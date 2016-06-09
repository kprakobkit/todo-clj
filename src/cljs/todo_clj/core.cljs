(ns todo-clj.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST DELETE]]
            [cljs.core.async :refer [put! chan <!]]))

(enable-console-print!)

(defonce app-state (atom {:todos []}))

(defn trace [content]
  (println content))

(defn handler [response cursor]
  (om/update! cursor :todos response))


(defn remove-todo [todo handler] 
  (let [url (:url todo)]
    (DELETE url {:handler handler
                 :error-handler trace
                 :format :json})))

(defn todo-view [todo]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete]}]
      (dom/li #js {:key (str (:id todo) (rand)) :className "todo"}
              (:title todo)
              (dom/button #js {:className "todo__remove"
                               :onClick #(remove-todo todo (fn [_] (put! delete @todo)))} "x")))))

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
       :title ""})
    om/IWillMount
    (will-mount [_]
      (let [delete (om/get-state owner :delete)]
        (GET "/todos" {:handler #(handler % app)
                     :keywords? true
                     :response-format :json})
        (go (loop []
              (let [todo (<! delete)]
                (om/transact! app :todos
                   (fn [todos] (vec (remove #(=  todo %) todos))))
                (recur))))))
    om/IRenderState
    (render-state [this {:keys [delete title]}]
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
                        :init-state {:delete delete}}))
           (:todos app)))))))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})
