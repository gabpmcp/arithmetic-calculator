(ns arithmetic-calculator.interceptors.cors)

(def cors-interceptor
  {:name ::cors-interceptor
   :enter (fn [context]
            ;; Agrega cabeceras CORS a la respuesta para todas las solicitudes
            (update-in context [:response :headers]
                       merge {"Access-Control-Allow-Origin"  "http://localhost:3000"
                              "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS"
                              "Access-Control-Allow-Headers" "Content-Type, Authorization"}))
   :leave (fn [context]
            ;; Si el método de la solicitud es OPTIONS, devolver una respuesta 200 OK
            (if (= (:request-method (:request context)) :options)
              (assoc context :response
                     {:status 200
                      :headers {"Access-Control-Allow-Origin"  "http://localhost:3000"
                                "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS"
                                "Access-Control-Allow-Headers" "Content-Type, Authorization"}
                      :body ""})
              context))})
