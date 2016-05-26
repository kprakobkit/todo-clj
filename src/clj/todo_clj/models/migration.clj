(ns todo-clj.models.migration
  (:require [clojure.java.jdbc :as sql]
            [todo-clj.models.todo :as todo]))

(defn migrated? []
  (-> (sql/query todo/spec
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='todos'")])
      first :count pos?))

(defn migrate []
  (when (not (migrated?))
    (print "Creating database structure...") (flush)
    (sql/db-do-commands todo/spec
                        (sql/create-table-ddl
                         :todos
                         [[:id :serial "PRIMARY KEY"]
                          [:name :text]])))
  (println " done"))

(defn populate []
  (sql/insert! todo/spec :todos {:name "buy eggs"}))
