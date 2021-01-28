(ns edd.json
  (:require [clojure.walk :refer [postwalk]]
            [clojure.string :as str]
            [ajax.json :as ajax-json]))

(defn parse-custom-fields
  [edn]
  (postwalk (fn [x]
              (cond
                (and (string? x)
                     (str/starts-with? x ":")) (keyword (subs x 1))
                (and (string? x)
                     (str/starts-with? x "#")) (uuid (subs x 1))
                :else x))
            edn))

(defn fmap [f m]
  (into (empty m)
        (if (map? m)
          (for [[k v] m]
            [k (f v)])
          (for [k m]
            (f k)))))

(defn convert
  [x]
  (cond
    (keyword? x) (str ":" (name x))
    (uuid? x) (str "#" x)
    (coll? x) (fmap convert x)
    :else x))

(defn encode-custom-fields
  [edn]
  (fmap convert edn))

(defn custom-json-parser
  [& params]
  (-> (apply ajax-json/read-json-native params)
      (parse-custom-fields)))

(def custom-response-format
  (ajax-json/make-json-response-format
    custom-json-parser))