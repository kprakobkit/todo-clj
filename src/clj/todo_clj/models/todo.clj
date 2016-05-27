(ns todo-clj.models.todo
  (:require [clojure.java.jdbc :as sql]))

(def spec (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/todo-clj"))

(defn all []
  (let [todos (sql/query spec ["select * from todos"])]
    (vec todos)))

(defn create [{title :title}]
  (first (sql/insert! spec :todos {:title title})))

(defn find-by-id [id]
  (first (sql/query spec ["select * from todos where id = ?::integer" id])))

(defn update-todo [id attr]
  (sql/update! spec :todos attr ["id = ?::integer" id])
  (find-by-id id))

(defn delete [id]
  (sql/delete! spec :todos ["id = ?::integer" id]))

(defn delete-all []
  (sql/delete! spec :todos []))
