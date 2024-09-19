(ns arithmetic-calculator.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(def db-spec {:dbtype "h2:mem" :dbname "calc_db"})

(def datasource (jdbc/get-datasource db-spec))

(defn initialize-db []
  (jdbc/execute! datasource ["CREATE TABLE users (
                               id UUID PRIMARY KEY,
                               username VARCHAR(255) UNIQUE NOT NULL,
                               password VARCHAR(255) NOT NULL,
                               status VARCHAR(50) NOT NULL,
                               balance DECIMAL(10,2) NOT NULL DEFAULT 100.00
                             )"])
  (jdbc/execute! datasource ["CREATE TABLE operations (
                               id UUID PRIMARY KEY,
                               type VARCHAR(50) NOT NULL,
                               cost DECIMAL(10,2) NOT NULL
                             )"])
  (jdbc/execute! datasource ["CREATE TABLE records (
                               id UUID PRIMARY KEY,
                               operation_id UUID NOT NULL,
                               user_id UUID NOT NULL,
                               amount DECIMAL(10,2) NOT NULL,
                               user_balance DECIMAL(10,2) NOT NULL,
                               operation_response VARCHAR(255),
                               date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               is_deleted BOOLEAN DEFAULT FALSE,
                               FOREIGN KEY (operation_id) REFERENCES operations(id),
                               FOREIGN KEY (user_id) REFERENCES users(id)
                             )"])
  ;; Insertar operaciones
  (doseq [op [{:id      (java.util.UUID/randomUUID)
               :type    "addition"
               :cost    1.0}
              {:id      (java.util.UUID/randomUUID)
               :type    "subtraction"
               :cost    1.0}
              {:id      (java.util.UUID/randomUUID)
               :type    "multiplication"
               :cost    2.0}
              {:id      (java.util.UUID/randomUUID)
               :type    "division"
               :cost    2.0}
              {:id      (java.util.UUID/randomUUID)
               :type    "square_root"
               :cost    3.0}
              {:id      (java.util.UUID/randomUUID)
               :type    "random_string"
               :cost    5.0}]]
    (sql/insert! datasource :operations op)))
