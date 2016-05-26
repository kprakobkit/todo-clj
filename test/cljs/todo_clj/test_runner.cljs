(ns todo-clj.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [todo-clj.core-test]))

(enable-console-print!)

(doo-tests 'todo-clj.core-test)
