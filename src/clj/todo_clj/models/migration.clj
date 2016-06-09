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
                          [:title :text]
                          [:completed :boolean :default :false]
                          [:position :integer]])))
  (println " done"))

(defn drop-db []
  (sql/db-do-commands todo/spec
                      (sql/drop-table-ddl :todos)))

(defn populate []
  (sql/insert! todo/spec :todos {:title "buy eggs" :position 1 :completed true})
  (sql/insert! todo/spec :todos {:title "file tax return" :position 3}))
