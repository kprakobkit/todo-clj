(ns todo-clj.models.todo
  (:require [clojure.java.jdbc :as sql]
            [clojure.set :refer [rename-keys]]))

(def spec (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/todo-clj"))

(defn order-to-position [todo]
  (rename-keys todo {:order :position}))

(defn position-to-order [todo]
  (rename-keys todo {:position :order}))

(defn add-url [todo]
  (assoc todo :url (str "/todos/" (:id todo))))

(defn parse [todo]
  (-> todo
      position-to-order
      add-url))

(defn all []
  (vec (sql/query spec ["select * from todos"] {:row-fn parse})))

(defn create [todo]
  (parse (first (sql/insert! spec :todos (order-to-position todo)))))

(defn find-by-id [id]
  (first (sql/query spec ["select * from todos where id = ?::integer" id] {:row-fn parse})))

(defn update-todo [id todo]
  (sql/update! spec :todos (order-to-position todo) ["id = ?::integer" id])
  (find-by-id id))

(defn delete [id]
  (sql/delete! spec :todos ["id = ?::integer" id]))

(defn delete-all []
  (sql/delete! spec :todos []))
