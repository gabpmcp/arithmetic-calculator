(ns arithmetic-calculator.handlers
  (:require [arithmetic-calculator.db :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [ring.util.response :as response]
            [buddy.hashers :as hashers]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends.token :refer [token-backend]]
            [clojure.data.json :as json]
            [clojure.string :as str]))

(def secret-token "super-secret-token")

;; Middleware de autenticación
(def auth-backend (token-backend {:authfn (fn [req token]
                                            (let [user (sql/find-by-keys db/datasource :users {:token token})]
                                              (first user)))}))

;; Función para crear un nuevo usuario (para fines de prueba)
(defn register-user [request]
  (let [{:keys [username password]} (:json-params request)
        hashed-pw (hashers/derive password)]
    (try
      (sql/insert! db/datasource :users {:id       (java.util.UUID/randomUUID)
                                         :username username
                                         :password hashed-pw
                                         :status   "active"
                                         :balance  100.00})
      (response/response {:status "User registered successfully"})
      (catch Exception e
        (response/status (response/response {:error "Username already exists"}) 400)))))

;; Función de autenticación
(defn login [request]
  (let [{:keys [username password]} (:json-params request)
        user (first (sql/find-by-keys db/datasource :users {:username username}))]
    (if (and user (hashers/check password (:password user)))
      (let [token (str (java.util.UUID/randomUUID))]
        (sql/update! db/datasource :users {:token token} {:id (:id user)})
        (response/response {:token token}))
      (response/status (response/response {:error "Invalid credentials"}) 401))))

;; Obtener operaciones disponibles
(defn get-operations [request]
  (let [operations (sql/query db/datasource ["SELECT * FROM operations"])]
    (response/response operations)))

;; Realizar una operación
(defn perform-operation [request]
  (let [user        (:identity request)
        {:keys [operation_type operands]} (:json-params request)
        operation   (first (sql/find-by-keys db/datasource :operations {:type operation_type}))
        cost        (:cost operation)
        user-balance (:balance user)]
    (if (>= user-balance cost)
      (let [result (case operation_type
                     "addition"       (apply + operands)
                     "subtraction"    (apply - operands)
                     "multiplication" (apply * operands)
                     "division"       (apply / operands)
                     "square_root"    (Math/sqrt (first operands))
                     "random_string"  (slurp "https://www.random.org/strings/?num=1&len=10&digits=on&upperalpha=on&loweralpha=on&unique=on&format=plain&rnd=new"))
            new-balance (- user-balance cost)]
        ;; Actualizar saldo de usuario
        (sql/update! db/datasource :users {:balance new-balance} {:id (:id user)})
        ;; Registrar operación
        (sql/insert! db/datasource :records {:id                 (java.util.UUID/randomUUID)
                                             :operation_id       (:id operation)
                                             :user_id            (:id user)
                                             :amount             cost
                                             :user_balance       new-balance
                                             :operation_response (str result)})
        (response/response {:result      result
                            :new_balance new-balance}))
      (response/status (response/response {:error "Insufficient balance"}) 400))))

;; Obtener registros del usuario con paginación y filtrado
(defn get-records [request]
  (let [user       (:identity request)
        params     (:query-params request)
        page       (Integer/parseInt (get params "page" "1"))
        per-page   (Integer/parseInt (get params "per_page" "10"))
        offset     (* (dec page) per-page)
        search     (get params "search" "")
        sort-by    (get params "sort_by" "date")
        order      (get params "order" "desc")
        valid-sort-columns #{"date" "amount" "operation_response"}
        sort-column (if (valid-sort-columns sort-by) sort-by "date")
        order-clause (if (= (str/lower-case order) "asc") "ASC" "DESC")
        total-count (-> (jdbc/execute-one! db/datasource
                                           ["SELECT COUNT(*) AS count FROM records WHERE user_id = ? AND is_deleted = FALSE AND operation_response LIKE ?"
                                            (:id user) (str "%" search "%")])
                        :count)
        records    (jdbc/execute! db/datasource
                                  [(str "SELECT * FROM records WHERE user_id = ? AND is_deleted = FALSE AND operation_response LIKE ? "
                                        "ORDER BY " sort-column " " order-clause " LIMIT ? OFFSET ?")
                                   (:id user) (str "%" search "%") per-page offset])]
    (response/response {:total      total-count
                        :page       page
                        :per_page   per-page
                        :records    records})))

;; Eliminar (lógicamente) un registro
(defn delete-record [request]
  (let [user      (:identity request)
        record-id (get-in request [:path-params :id])
        record    (first (sql/find-by-keys db/datasource :records {:id record-id :user_id (:id user)}))]
    (if record
      (do
        (sql/update! db/datasource :records {:is_deleted true} {:id record-id})
        (response/response {:status "Record deleted"}))
      (response/status (response/response {:error "Record not found"}) 404))))

(defn get-user-balance [request]
  (let [user (:identity request)]
    (response/response {:balance (:users/balance user)})))

(defn options-handler
  "Handler para solicitudes OPTIONS"
  [request]
  {:status 200
   :headers {"Allow" "OPTIONS, GET, POST, PUT, DELETE"}
   :body ""})