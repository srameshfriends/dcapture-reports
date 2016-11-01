--
--
--createPaymentTable
CREATE TABLE IF NOT EXISTS entity.payment (code VARCHAR(8), payment_index INTEGER, payment_date DATE,
 description VARCHAR(256), expense_item VARCHAR(8), expense_currency VARCHAR(8), expense_amount DECIMAL,
 exchange_rate DECIMAL, payment_currency VARCHAR(8), payment_amount DECIMAL,
 payment_account VARCHAR(8), status VARCHAR(32), PRIMARY KEY (code));
--
