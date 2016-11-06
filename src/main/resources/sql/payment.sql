--
--
--createPaymentTable
CREATE TABLE IF NOT EXISTS entity.payment (code VARCHAR(8), payment_index INTEGER, payment_date DATE,
 description VARCHAR(256), expense_item VARCHAR(8), expense_currency VARCHAR(8), expense_amount DECIMAL,
 exchange_rate DECIMAL, payment_currency VARCHAR(8), payment_amount DECIMAL,
 payment_account VARCHAR(8), status VARCHAR(16), PRIMARY KEY (code));
--insertPayment
INSERT INTO entity.payment (code, payment_index, payment_date, description, expense_item, expense_currency,
expense_amount, exchange_rate, payment_currency, payment_amount, payment_account, status)
 VALUES(?,?,?,?,?,?,?,?,?,?,?,?);
--
