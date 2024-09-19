(ns arithmetic-calculator.routes
  (:require [arithmetic-calculator.handlers :as handlers]
            [io.pedestal.http.route :as route]))

(defn options-handler
  "Handler para solicitudes OPTIONS"
  [request]
  {:status 200
   :headers {"Allow" "OPTIONS, GET, POST, PUT, DELETE"}
   :body ""})

(defn routes []
  (route/expand-routes
   #{["/register" :post handlers/register-user :route-name :register]
     ["/login"    :post handlers/login :route-name :login]
     ["/operations" :get handlers/get-operations :route-name :get-operations]
     ["/records"  :post handlers/perform-operation :route-name :perform-operation]
     ["/records"  :get handlers/get-records :route-name :get-records]
     ["/records/:id" :delete handlers/delete-record :route-name :delete-record]
     ["/user/balance" :get handlers/get-user-balance :route-name :get-user-balance]
     ;;Functions for CORS
     ["/register" :options options-handler, :route-name :options-register]
    ;;  ["/login" :options options-handler]
    ;;  ["/operations" :options options-handler]
    ;;  ["/records" :options options-handler]
    ;;  ["/records/:id" :options options-handler]
    ;;  ["/user/balance" :options options-handler]
     }))