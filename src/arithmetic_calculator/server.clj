(ns arithmetic-calculator.server
  (:gen-class) ; Para permitir ejecutar como programa principal
  (:require [io.pedestal.http :as http]
            [arithmetic-calculator.routes :as routes]
            [arithmetic-calculator.db :as db]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [arithmetic-calculator.handlers :as handlers]))

(defn service []
  {:env                     :prod
   ::http/routes            (routes/routes)
   ::http/resource-path     "/public"
   ::http/type              :jetty
   ::http/port              8080
   ::http/allowed-origins   (constantly true)
   ::http/enable-cors       true
   ::http/secure-headers    {:content-security-policy-settings {:object-src "'none'"}}
   ::http/enable-session    {:cookie-name "SESSIONID"}
   ::http/allowed-methods   [:get :post :put :delete]
   ::http/interceptors      [(wrap-json-body {:keywords? true})
                             wrap-json-response
                             (wrap-authentication handlers/auth-backend)]})

(defn -main [& args]
  (db/initialize-db)
  (let [server (http/create-server (service))]
    (http/start server)))
