(ns arithmetic-calculator.interceptors.auth
  (:require [buddy.sign.jwt :as auth]
            [buddy.auth.backends.token :refer [jws-backend]]
            [environ.core :refer [env]]))

(def secret-key (env :secret-key))

(def auth-backend
  (jws-backend {:secret secret-key
                :options {:alg :hs512}}))

(defn auth-interceptor []
  {:name ::auth-interceptor
   :enter (fn [context]
            (let [request (:request context)
                  auth-header (get-in request [:headers "authorization"])]
              ;; Verificar que el header `Authorization` está presente y tiene el formato adecuado
              (if (and auth-header
                       (re-matches #"(?i)^Bearer\s+(.+)$" auth-header))
                ;; Extraer el token y autenticar
                (let [token (second (re-matches #"(?i)^Bearer\s+(.+)$" auth-header))
                      user (auth/unsign (assoc-in request [:headers "authorization"] token) auth-backend)]
                  (if user
                    ;; Usuario autenticado, agregarlo al contexto
                    (assoc-in context [:request :user] user)
                    ;; Token inválido o usuario no autenticado
                    (assoc-in context [:response :status] 401)))
                ;; Header `Authorization` ausente o en formato incorrecto
                (assoc-in context [:response :status] 401))))
   :leave identity})
