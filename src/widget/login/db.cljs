(ns widget.login.db
  (:require [widget.login.core :as core]))

(def default-db
  {::name                    "Home page"
   ::username                ""
   ::password                ""
   ::confirmation-code       ""
   ::confirmation-visible    false
   ::error-message-visible   false
   ::error-message           ""
   ::form-type               :login
   ::forgot-password-visible false
   ::dialog-visible          false})
