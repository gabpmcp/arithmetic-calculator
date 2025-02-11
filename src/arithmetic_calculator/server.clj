(ns arithmetic-calculator.server
  (:require [arithmetic-calculator.db :as db]
            [arithmetic-calculator.interceptors.auth :refer [auth-interceptor]]
            [arithmetic-calculator.interceptors.cors :refer [cors-interceptor]]
            [arithmetic-calculator.routes :as routes]
            [io.pedestal.http :as http]))

(def service
  {:env                     :prod
   ::http/routes            (routes/routes)
   ::http/resource-path     "/public"
   ::http/type              :jetty
   ::http/port              8080
   ::http/secure-headers    {:content-security-policy-settings {:object-src "'none'"}}
   ::http/enable-session    {:cookie-name "SESSIONID"}
   ::http/interceptors      [cors-interceptor ;; Agregar el interceptor de CORS
                             (auth-interceptor) ;; Interceptor de autenticación
                             http/json-body
                             http/json-response
                             routes/routes]})

(defn -main [& args]
  (db/initialize-db)
  (let [server (http/create-server service)]
    (http/start server)))
