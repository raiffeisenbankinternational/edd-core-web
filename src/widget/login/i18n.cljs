(ns widget.login.i18n)

(def tr
  {:confirmation-code      {:en "Confirmation code"
                            :de "Bestätigungscode"}
   :resend-code            {:en "Resend code"
                            :de "Neue code"}

   :reset-password         {:en "Reset password"
                            :de "Passwort zurücksetzen"}
   :confirm-password-reset {:en "New passwod"
                            :de "Neues Kenwort"}
   :confirm-reset-password {:en "Confirm"
                            :de "Bestätigen"}
   :login                  {:en "Login"
                            :de "Anmelden"}
   :username               {:en "Username"
                            :de "Benutzername"}
   :password               {:en "Password"
                            :de "Kennwort"}
   :logout                 {:en "Logout"
                            :de "Abmelden"}
   :register               {:en "Register"
                            :de "Registrieren"}
   :cancel                 {:en "Cancel"
                            :de "Abbrechen"}
   :forgot-password        {:en "Forgot password?"
                            :de "Kennwort vergessen?"}
   :confirm-login          {:en "Please check your email"
                            :de "Bitte überprüfen Sie Ihre E-Mail"}

   :invalid-password       {:en "Invalid password. Must contian uppercase characters, numbers and minimum 8 characters. "
                            :de "Ungültiges Kennwort. Muss Großbuchstaben, Zahlen und mindestens 8 Zeichen enthalten."}

   :user-exists            {:en "User exists"
                            :de "Benutzer existiert"}
   :invalid-email          {:en "Invalid email"
                            :de "Ungültige E-Mail"}
   :invalid-credentials    {:en "Invalid username or password"
                            :de "Benutzername oder Kennwort ungültig"}
   :code-expired           {:en "Code expired"
                            :de "Code abgelaufen"}
   :attempt-limit-exceeded {:en "Attempt limit exceeded. Please wait 15 min. "
                            :de "Versuchslimit überschritten. Bitte warten Sie 15 min."}
   :invalid-code           {:en "Invalid code"
                            :de "Ungültige code"}
   :missing-username       {:en "Missing username"
                            :de "Fehlender Benutzername"}
   :missing-password       {:en "Missing password"
                            :de "Fehlender Kennwort"}})
