package excel.accounting.service;

import excel.accounting.dao.PaymentDao;
import excel.accounting.db.EntityToRowColumns;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.Transaction;
import excel.accounting.entity.Payment;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.DataValidator;
import excel.accounting.shared.StringRules;
import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment Service
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class PaymentService extends AbstractService implements ExcelTypeConverter<Payment>,
        EntityToRowColumns<Payment> {
    private PaymentDao paymentDao;

    private PaymentDao getPaymentDao() {
        if (paymentDao == null) {
            paymentDao = (PaymentDao) getBean("paymentDao");
        }
        return paymentDao;
    }

    @Override
    protected String getSqlFileName() {
        return "payment";
    }

    private boolean insertValid(int index, Payment payment) {
        StringBuilder builder = new StringBuilder();
        if (StringRules.isEmpty(payment.getCode())) {
            builder.append("Code Empty");
        }
        if (payment.getPaymentDate() == null) {
            builder.append("Payment Date not found");
        }
        if (StringRules.isEmpty(payment.getDescription())) {
            builder.append("Description Empty");
        }
        if (builder.length() != 0) {
            setMessage("Line : " + index + " \t " + builder.toString());
            return false;
        }
        return true;
    }

    private boolean confirmValidPayment(Payment payment) {
        return true;
    }

    public void insertPayment(List<Payment> paymentList) {
        List<Payment> validList = new ArrayList<>();
        int index = 0;
        for(Payment payment : paymentList) {
            if(insertValid(index + 1, payment)) {
                validList.add(payment);
            }
            index += 1;
        }
        if (validList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("insertPayment");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Payment payment : paymentList) {
            transaction.addBatch(getColumnsMap("insertPayment", payment));
        }
        executeBatch(transaction);
    }

    public void setAsConfirmed(List<Payment> paymentList) {
        List<Payment> filteredList = filteredByStatus(Status.Drafted, paymentList);
        if (filteredList.isEmpty()) {
            return;
        }
        List<Payment> validList = filteredList.stream().filter(this::confirmValidPayment).collect(Collectors.toList());
        if (validList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Payment payment : validList) {
            transaction.addBatch(getColumnsMap("updateStatus", payment));
        }
        executeBatch(transaction);
    }

    public void setAsDrafted(List<Payment> paymentList) {
        updateStatus(Status.Confirmed, Status.Drafted, paymentList);
    }

    public void setAsClosed(List<Payment> paymentList) {
        updateStatus(Status.Confirmed, Status.Closed, paymentList);
    }

    private void updateStatus(Status requiredStatus, Status changedStatus, List<Payment> paymentList) {
        List<Payment> filteredList = filteredByStatus(requiredStatus, paymentList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (Payment payment : filteredList) {
            payment.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Payment payment : filteredList) {
            transaction.addBatch(getColumnsMap("updateStatus", payment));
        }
        executeBatch(transaction);
    }

    public void deletePayment(List<Payment> paymentList) {
        List<Payment> filteredList = filteredByStatus(Status.Drafted, paymentList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deletePayment");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Payment payment : filteredList) {
            transaction.addBatch(getColumnsMap("deletePayment", payment));
        }
        executeBatch(transaction);
    }

    @Override
    public Map<Integer, Object> getColumnsMap(String queryName, Payment payment) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertPayment".equals(queryName)) {
            map.put(1, payment.getCode());
            map.put(2, payment.getPaymentIndex());
            map.put(3, payment.getPaymentDate());
            map.put(4, payment.getDescription());
            map.put(5, payment.getExpenseItem());
            map.put(6, payment.getExpenseCurrency());
            map.put(7, payment.getExpenseAmount());
            map.put(8, payment.getExchangeRate());
            map.put(9, payment.getPaymentCurrency());
            map.put(10, payment.getPaymentAmount());
            map.put(11, payment.getPaymentAccount());
            map.put(12, Status.Drafted.toString());
        } else if ("deletePayment".equals(queryName)) {
            map.put(1, payment.getCode());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, payment.getStatus().toString());
            map.put(2, payment.getCode());
        }
        return map;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Code", "Instalment", "Date", "Description", "Expense Item", "Expense Currency",
                "Expense Amount", "Exchange Rate", "Payment Currency", "Payment Amount", "Payment Account", "Status"};
    }

    @Override
    public Payment getExcelType(String type, Cell[] array) {
        Payment payment = new Payment();
        payment.setCode(DataConverter.getString(array[0]));
        payment.setPaymentIndex(DataConverter.getInteger(array[1]));
        payment.setPaymentDate(DataConverter.getDate(array[2]));
        payment.setDescription(DataConverter.getString(array[3]));
        payment.setExpenseItem(DataConverter.getString(array[4]));
        payment.setExpenseCurrency(DataConverter.getString(array[5]));
        payment.setExpenseAmount(DataConverter.getBigDecimal(array[6]));
        payment.setExchangeRate(DataConverter.getBigDecimal(array[7]));
        payment.setPaymentCurrency(DataConverter.getString(array[8]));
        payment.setPaymentAmount(DataConverter.getBigDecimal(array[9]));
        payment.setPaymentAccount(DataConverter.getString(array[10]));
        payment.setStatus(DataConverter.getStatus(array[11]));
        return payment;
    }

    @Override
    public Object[] getExcelRow(String type, Payment payment) {
        Object[] objects = new Object[12];
        objects[0] = payment.getCode();
        objects[1] = payment.getPaymentIndex();
        objects[2] = payment.getPaymentDate();
        objects[3] = payment.getDescription();
        objects[4] = payment.getExpenseItem();
        objects[5] = payment.getExpenseCurrency();
        objects[6] = payment.getExpenseAmount();
        objects[7] = payment.getExchangeRate();
        objects[8] = payment.getPaymentCurrency();
        objects[9] = payment.getPaymentAmount();
        objects[10] = payment.getPaymentAccount();
        objects[11] = payment.getStatus().toString();
        return objects;
    }

    private List<Payment> filteredByStatus(Status status, List<Payment> payList) {
        return payList.stream().filter(pay -> status.equals(pay.getStatus())).collect(Collectors.toList());
    }
}