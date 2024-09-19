(ns arithmetic-calculator.routes
  (:require [io.pedestal.http.route :as route]
            [arithmetic-calculator.handlers :as handlers]))

(defn routes []
  (route/expand-routes
   #{["/register" :post handlers/register-user :route-name :register]
     ["/login"    :post handlers/login :route-name :login]
     ["/operations" :get handlers/get-operations :route-name :get-operations]
     ["/records"  :post handlers/perform-operation :route-name :perform-operation]
     ["/records"  :get handlers/get-records :route-name :get-records]
     ["/records/:id" :delete handlers/delete-record :route-name :delete-record]}))
