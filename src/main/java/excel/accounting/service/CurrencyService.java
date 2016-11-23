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
public class CurrencyService extends AbstractService implements ExcelTypeConverter<Currency> {
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

    private void updateStatus(List<Currency> currencyList, int pid, SqlWriter handler) {
        try {
            List<SqlQuery> queryList = new ArrayList<>();
            for (Currency currency : currencyList) {
                QueryTool tool = createSqlBuilder(pid);
                tool.update("currency").updateColumns("status", currency.getStatus()).where("code", currency.getCode());
                queryList.add(tool.getSqlQuery());
            }
            executeCommit(150, queryList, handler);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setAsDrafted(List<Currency> currencyList, int pid, SqlWriter writer) {
        List<Currency> filteredList = filteredByStatus(Status.Confirmed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : confirmed currency are allowed to modify as drafted");
            return;
        }
        for (Currency currency : filteredList) {
            Object errorMessage = getCurrencyDao().findReferenceUsed(currency);
            if (errorMessage != null) {
                setMessage(errorMessage.toString());
                return;
            }
            currency.setStatus(Status.Drafted);
        }
        updateStatus(filteredList, pid, writer);
    }

    public void setAsConfirmed(List<Currency> currencyList, int pid, SqlWriter writer) {
        List<Currency> filteredList = filteredByStatus(Status.Drafted, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : drafted currency are allowed to modify as confirmed");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Confirmed);
        }
        updateStatus(filteredList, pid, writer);
    }

    public void setAsClosed(List<Currency> currencyList, int pid, SqlWriter writer) {
        List<Currency> filteredList = filteredByStatus(Status.Confirmed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : confirmed currency are allowed to modify as closed");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Closed);
        }
        updateStatus(filteredList, pid, writer);
    }

    public void reopenCurrency(List<Currency> currencyList, int pid, SqlWriter writer) {
        List<Currency> filteredList = filteredByStatus(Status.Closed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : closed currency are allowed to reopen");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Confirmed);
        }
        updateStatus(filteredList, pid, writer);
    }

    private boolean insertValid(Currency currency, StringRules rules) {
        return rules.isValid(currency.getCode()) && !StringRules.isEmpty(currency.getName());
    }

    public void insertCurrency(List<Currency> currencyList, int pid, SqlWriter writer) {
        setMessage("Currency code, name should not be empty");
        StringRules rules = new StringRules();
        rules.setMinMaxLength(3, 3);
        rules.setRulesType(RulesType.AlphaOnly);
        //
        List<Currency> validList = currencyList.stream().filter(currency ->
                insertValid(currency, rules)).collect(Collectors.toList());
        if (validList.isEmpty()) {
            setMessage("Valid currency not found");
        } else {
            insert(currencyList, pid, writer);
        }
    }

    public void deleteCurrency(List<Currency> currencyList, int pid, SqlWriter writer) {
        List<Currency> filteredList = filteredByStatus(Status.Drafted, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Error : Only drafted currency allowed to delete");
        } else {
            delete(currencyList, pid, writer);
        }
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Code", "Name", "Precision", "Symbol", "Status"};
    }

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
