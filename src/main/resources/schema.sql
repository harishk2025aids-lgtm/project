-- =====================================================================
-- Stage 1: Rural Health Network Platform - MySQL Schema
-- Run this manually with: mysql -u root -p < schema.sql
-- (Optional if using spring.jpa.hibernate.ddl-auto=update, which will
--  create these tables automatically from the JPA entities instead.)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS rural_health_db;
USE rural_health_db;

-- -------------------- CLINICAL DOMAIN --------------------

CREATE TABLE IF NOT EXISTS departments (
    department_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    location        VARCHAR(150),
    monthly_budget  DECIMAL(14,2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS doctors (
    doctor_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(120) NOT NULL,
    specialization  VARCHAR(100),
    phone           VARCHAR(20),
    email           VARCHAR(120),
    department_id   BIGINT,
    CONSTRAINT fk_doctor_department FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE IF NOT EXISTS patients (
    patient_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(120) NOT NULL,
    date_of_birth   DATE,
    gender          VARCHAR(10),
    phone           VARCHAR(20),
    address         VARCHAR(255),
    blood_group     VARCHAR(5),
    emergency_contact VARCHAR(20),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS consultations (
    consultation_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT NOT NULL,
    doctor_id           BIGINT,
    department_id       BIGINT,
    symptoms            TEXT,
    heart_rate          INT,
    systolic_bp         INT,
    diastolic_bp        INT,
    temperature_celsius DECIMAL(4,1),
    spo2                INT,
    respiratory_rate    INT,
    triage_score        DECIMAL(5,2),
    triage_priority     VARCHAR(20),
    status              VARCHAR(20) DEFAULT 'WAITING',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_consult_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    CONSTRAINT fk_consult_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
    CONSTRAINT fk_consult_department FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

-- -------------------- BILLING DOMAIN --------------------

CREATE TABLE IF NOT EXISTS products (
    product_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    category        VARCHAR(80),
    unit_price      DECIMAL(12,2) NOT NULL,
    is_service      BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS invoices (
    invoice_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    consultation_id BIGINT,
    invoice_date    DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount    DECIMAL(14,2) DEFAULT 0,
    status          VARCHAR(20) DEFAULT 'UNPAID',
    CONSTRAINT fk_invoice_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    CONSTRAINT fk_invoice_consultation FOREIGN KEY (consultation_id) REFERENCES consultations(consultation_id)
);

CREATE TABLE IF NOT EXISTS invoice_items (
    invoice_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id      BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(12,2) NOT NULL,
    line_total      DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id      BIGINT NOT NULL,
    amount_paid     DECIMAL(14,2) NOT NULL,
    payment_method  VARCHAR(30),
    payment_date    DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id)
);

-- -------------------- PROCUREMENT DOMAIN --------------------

CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    contact_person  VARCHAR(120),
    phone           VARCHAR(20),
    email           VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    purchase_order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id       BIGINT NOT NULL,
    order_date        DATETIME DEFAULT CURRENT_TIMESTAMP,
    status            VARCHAR(20) DEFAULT 'PENDING',
    total_amount      DECIMAL(14,2) DEFAULT 0,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);

CREATE TABLE IF NOT EXISTS purchase_items (
    purchase_item_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id        BIGINT NOT NULL,
    quantity          INT NOT NULL,
    unit_cost         DECIMAL(12,2) NOT NULL,
    line_total        DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_pi_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id),
    CONSTRAINT fk_pi_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- -------------------- FINANCE DOMAIN --------------------

CREATE TABLE IF NOT EXISTS budgets (
    budget_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id     BIGINT NOT NULL,
    fiscal_year       INT NOT NULL,
    fiscal_month      INT NOT NULL,
    allocated_amount  DECIMAL(14,2) NOT NULL,
    spent_amount      DECIMAL(14,2) DEFAULT 0,
    CONSTRAINT fk_budget_department FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE IF NOT EXISTS chart_of_accounts (
    account_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_code    VARCHAR(20) NOT NULL UNIQUE,
    account_name    VARCHAR(150) NOT NULL,
    account_type    VARCHAR(20) NOT NULL -- ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
);

CREATE TABLE IF NOT EXISTS journal_entries (
    journal_entry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_date       DATETIME DEFAULT CURRENT_TIMESTAMP,
    description      VARCHAR(255),
    reference_type   VARCHAR(30),   -- INVOICE, PAYMENT, PURCHASE_ORDER, MANUAL
    reference_id     BIGINT
);

CREATE TABLE IF NOT EXISTS journal_lines (
    journal_line_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    account_id       BIGINT NOT NULL,
    debit_amount     DECIMAL(14,2) DEFAULT 0,
    credit_amount    DECIMAL(14,2) DEFAULT 0,
    CONSTRAINT fk_jl_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(journal_entry_id),
    CONSTRAINT fk_jl_account FOREIGN KEY (account_id) REFERENCES chart_of_accounts(account_id)
);

-- -------------------- SEED DATA --------------------

INSERT INTO chart_of_accounts (account_code, account_name, account_type) VALUES
('1000', 'Cash', 'ASSET'),
('1100', 'Accounts Receivable', 'ASSET'),
('2000', 'Accounts Payable', 'LIABILITY'),
('3000', 'Owner Equity', 'EQUITY'),
('4000', 'Consultation Revenue', 'REVENUE'),
('4100', 'Diagnostic Service Revenue', 'REVENUE'),
('5000', 'Medical Supplies Expense', 'EXPENSE'),
('5100', 'Departmental Operating Expense', 'EXPENSE')
ON DUPLICATE KEY UPDATE account_name = VALUES(account_name);
