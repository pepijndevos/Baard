(ns baard
  (:require [clojure.string :as s])
  (:use [clojure.core.match :only [match]]))

(defn pattern [req]
  (vec (cons
    (:request-method req)
    (remove #{""} (s/split (:uri req) #"/")))))

(defn delegate [handler & args]
  (fn [req]
    (apply handler req args)))

(defn symbols [pattern]
  (filter #(and
             (symbol? %)
             (not= '& %)
             (not (resolve %)))
          (flatten pattern)))

(defn parse-match [[pattern action]]
  [pattern
   `(delegate ~action ~@(symbols pattern))])

(defmacro app [& routes]
  `(fn [req#]
     ((match (pattern req#)
        ~@(mapcat
            parse-match
            (partition 2 routes)))
        req#)))

