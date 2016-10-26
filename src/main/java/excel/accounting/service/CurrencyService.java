package excel.accounting.service;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowTypeConverter;
import excel.accounting.db.Transaction;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Currency Service
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class CurrencyService extends AbstractService implements RowTypeConverter<Currency>, ExcelTypeConverter<Currency> {

    @Override
    protected String getSqlFileName() {
        return "currency";
    }

    public List<Currency> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<String> findCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findCodeList");
        return getDataReader().findString(queryBuilder);
    }

    private void updateStatus(Status requiredStatus, Status changedStatus, List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(requiredStatus, currencyList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Currency currency : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, currency));
        }
        transaction.executeBatch();
    }

    public void setAsDrafted(List<Currency> currencyList) {
        updateStatus(Status.Confirmed, Status.Drafted, currencyList);
    }

    public void setAsConfirmed(List<Currency> currencyList) {
        updateStatus(Status.Drafted, Status.Confirmed, currencyList);
    }

    public void setAsClosed(List<Currency> currencyList) {
        updateStatus(Status.Confirmed, Status.Closed, currencyList);
    }

    public void insertCurrency(List<Currency> currencyList) {
        QueryBuilder queryBuilder = getQueryBuilder("insertCurrency");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Currency currency : currencyList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, currency));
        }
        transaction.executeBatch();
    }

    public void updateCurrency(List<Currency> currencyList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateCurrency");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Currency currency : currencyList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, currency));
        }
        transaction.executeBatch();
    }

    public void deleteCurrency(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Drafted, currencyList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteCurrency");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Currency currency : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, currency));
        }
        transaction.executeBatch();
    }

    /**
     * id, code, name, status, decimal_precision, symbol
     */
    @Override
    public Currency getRowType(QueryBuilder builder, Object[] objectArray) {
        Currency currency = new Currency();
        currency.setId((Integer) objectArray[0]);
        currency.setCode((String) objectArray[1]);
        currency.setName((String) objectArray[2]);
        currency.setStatus(DataConverter.getStatus(objectArray[3]));
        currency.setDecimalPrecision((Integer) objectArray[4]);
        currency.setSymbol((String) objectArray[5]);
        return currency;
    }

    /**
     * insertCurrency
     * code, name, status, decimal_precision, symbol
     * deleteCurrency
     * find by code
     * updateStatus
     * set status find by code
     * updateCurrency
     * code, name, decimal_precision, symbol find by code
     */
    @Override
    public Map<Integer, Object> getRowObjectMap(QueryBuilder builder, Currency type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertCurrency".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, Status.Drafted.toString());
            map.put(4, type.getDecimalPrecision());
            map.put(5, type.getSymbol());
        } else if ("deleteCurrency".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
        } else if ("updateStatus".equals(builder.getQueryName())) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        } else if ("updateCurrency".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, type.getDecimalPrecision());
            map.put(4, type.getSymbol());
            map.put(5, type.getCode());
        }
        return map;
    }

    /**
     * code, name, status, decimal_precision, symbol
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Code", "Name", "Status", "Precision", "Symbol"};
    }

    /**
     * code, name, status, precision, symbol
     */
    @Override
    public Currency getExcelType(String type, Cell[] array) {
        Currency currency = new Currency();
        currency.setCode(DataConverter.getString(array[0]));
        currency.setName(DataConverter.getString(array[1]));
        currency.setStatus(DataConverter.getStatus(array[2]));
        currency.setDecimalPrecision(DataConverter.getInteger(array[3]));
        currency.setSymbol(DataConverter.getString(array[4]));
        return currency;
    }

    /**
     * code, name, status, precision, symbol
     */
    @Override
    public Object[] getExcelRow(String type, Currency currency) {
        Object[] cellData = new Object[5];
        cellData[0] = currency.getCode();
        cellData[1] = currency.getName();
        cellData[2] = currency.getStatus().toString();
        cellData[3] = currency.getDecimalPrecision();
        cellData[4] = currency.getSymbol();
        return cellData;
    }

    private List<Currency> filteredByStatus(Status status, List<Currency> curList) {
        return curList.stream().filter(cur -> status.equals(cur.getStatus())).collect(Collectors.toList());
    }
}
