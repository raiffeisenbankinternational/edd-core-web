(ns components.util)

(defmacro lazy-component [the-sym spec-key spec-value]
  `(components.util/create-lazy-component
     (shadow.lazy/loadable ~the-sym)
     ~spec-value))