(ns todo-clj.models.todo
  (:require [clojure.java.jdbc :as sql]))

(def spec (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/todo-clj"))

(defn all []
  (let [todos (sql/query spec ["select * from todos"])]
    todos))
