package excel.accounting.service;

import excel.accounting.db.*;
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

    public List<Currency> searchCurrency(String searchText, Status status) {
        InClauseQuery inClauseQuery = new InClauseQuery(status.toString());
        QueryBuilder queryBuilder = getQueryBuilder("searchCurrency");
        queryBuilder.addInClauseQuery("$status", inClauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
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
     * code, name, status, decimal_precision, symbol
     */
    @Override
    public Currency getRowType(QueryBuilder builder, Object[] objectArray) {
        Currency currency = new Currency();
        currency.setCode((String) objectArray[0]);
        currency.setName((String) objectArray[1]);
        currency.setDecimalPrecision((Integer) objectArray[2]);
        currency.setSymbol((String) objectArray[3]);
        currency.setStatus(DataConverter.getStatus(objectArray[4]));
        return currency;
    }

    /**
     * insertCurrency
     * code, name, decimal_precision, symbol, status
     * deleteCurrency
     * find by code
     * updateStatus
     * set status find by code
     * updateCurrency
     * name, decimal_precision, symbol find by code
     */
    @Override
    public Map<Integer, Object> getRowObjectMap(QueryBuilder builder, Currency type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertCurrency".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, type.getDecimalPrecision());
            map.put(4, type.getSymbol());
            map.put(5, Status.Drafted.toString());
        } else if ("deleteCurrency".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
        } else if ("updateStatus".equals(builder.getQueryName())) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        } else if ("updateCurrency".equals(builder.getQueryName())) {
            map.put(1, type.getName());
            map.put(2, type.getDecimalPrecision());
            map.put(3, type.getSymbol());
            map.put(4, type.getCode());
        }
        return map;
    }

    /**
     * code, name, decimal_precision, symbol, status
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Code", "Name", "Precision", "Symbol", "Status"};
    }

    /**
     * code, name, precision, symbol, status
     */
    @Override
    public Currency getExcelType(String type, Cell[] array) {
        Currency currency = new Currency();
        currency.setCode(DataConverter.getString(array[0]));
        currency.setName(DataConverter.getString(array[1]));
        currency.setDecimalPrecision(DataConverter.getInteger(array[2]));
        currency.setSymbol(DataConverter.getString(array[3]));
        currency.setStatus(DataConverter.getStatus(array[4]));
        return currency;
    }

    /**
     * code, name, precision, symbol, status
     */
    @Override
    public Object[] getExcelRow(String type, Currency currency) {
        Object[] cellData = new Object[5];
        cellData[0] = currency.getCode();
        cellData[1] = currency.getName();
        cellData[2] = currency.getDecimalPrecision();
        cellData[3] = currency.getSymbol();
        cellData[4] = currency.getStatus().toString();
        return cellData;
    }

    private List<Currency> filteredByStatus(Status status, List<Currency> curList) {
        return curList.stream().filter(cur -> status.equals(cur.getStatus())).collect(Collectors.toList());
    }
}
