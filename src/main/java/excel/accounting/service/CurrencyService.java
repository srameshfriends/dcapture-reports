package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.CurrencyDao;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.RulesType;
import excel.accounting.shared.StringRules;
import org.apache.poi.ss.usermodel.Cell;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Currency Service
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class CurrencyService extends AbstractService implements EntityToRowColumns<Currency>,
        ExcelTypeConverter<Currency> {
    private CurrencyDao currencyDao;

    @Override
    protected String getSqlFileName() {
        return "currency";
    }

    private CurrencyDao getCurrencyDao() {
        if (currencyDao == null) {
            currencyDao = (CurrencyDao) getBean("currencyDao");
        }
        return currencyDao;
    }

    private void updateStatus(List<Currency> currencyList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Currency currency : currencyList) {
            transaction.addBatch(getColumnsMap("updateStatus", currency));
        }
        executeBatch(transaction);
    }

    public void setAsDrafted(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Confirmed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : confirmed currency are allowed to modify as drafted");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Drafted);
        }
        updateStatus(currencyList);
    }

    public void setAsConfirmed(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Drafted, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : drafted currency are allowed to modify as confirmed");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Confirmed);
        }
        updateStatus(currencyList);
    }

    public void setAsClosed(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Confirmed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : confirmed currency are allowed to modify as closed");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Closed);
        }
        updateStatus(currencyList);
    }

    public void reopenCurrency(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Closed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : closed currency are allowed to reopen");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Confirmed);
        }
        updateStatus(currencyList);
    }

    private boolean insertValid(Currency currency, StringRules rules) {
        return rules.isValid(currency.getCode()) && !StringRules.isEmpty(currency.getName());
    }

    public void insertCurrency(List<Currency> currencyList) {
        setMessage("Currency code, name should not be empty");
        StringRules rules = new StringRules();
        rules.setMinMaxLength(3, 3);
        rules.setRulesType(RulesType.AlphaOnly);
        //
        List<String> existingList = getCurrencyDao().findCodeList();
        List<Currency> validList = new ArrayList<>();
        for (Currency currency : currencyList) {
            if (insertValid(currency, rules) && !existingList.contains(currency.getCode())) {
                validList.add(currency);
            }
        }
        if(validList.isEmpty()) {
            setMessage("Valid currency not found");
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("insertCurrency");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Currency currency : validList) {
            transaction.addBatch(getColumnsMap("insertCurrency", currency));
        }
        executeBatch(transaction);
    }

    public void deleteCurrency(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Drafted, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Error : Only drafted currency allowed to delete");
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteCurrency");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Currency currency : filteredList) {
            transaction.addBatch(getColumnsMap("deleteCurrency", currency));
        }
        executeBatch(transaction);
    }

    /**
     * insertCurrency
     * code, name, decimal_precision, symbol, status
     * deleteCurrency
     * find by code
     * updateStatus
     * set status find by code
     */
    @Override
    public Map<Integer, Object> getColumnsMap(final String queryName, Currency entity) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertCurrency".equals(queryName)) {
            map.put(1, entity.getCode());
            map.put(2, entity.getName());
            map.put(3, entity.getDecimalPrecision());
            map.put(4, entity.getSymbol());
            map.put(5, Status.Drafted.toString());
        } else if ("deleteCurrency".equals(queryName)) {
            map.put(1, entity.getCode());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, entity.getStatus().toString());
            map.put(2, entity.getCode());
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
