package excel.accounting.service;

import excel.accounting.db.QueryBuilder;
import excel.accounting.entity.Account;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import excel.accounting.db.RowTypeConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * Account Service
 */
public class AccountService extends AbstractService implements RowTypeConverter<Account>, ExcelTypeConverter<Account> {

    @Override
    protected String getSqlFileName() {
        return "account.sql";
    }

    public List<Account> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
       // return getDataProcessor().findRowDataList(queryBuilder, this);
        return new ArrayList<>();
    }

    public void insertAccount(Account account) {
        QueryBuilder queryBuilder = getQueryBuilder("insertAccount");
        queryBuilder.add(1, account.getAccountNumber());
        queryBuilder.add(2, account.getName());
        System.out.println(queryBuilder.getQuery());
        getDataProcessor().insert(queryBuilder);
    }

    public void insertAccount(List<Account> accountList) {
        accountList.forEach(this::insertAccount);
    }


    @Override
    public Account getRowType(QueryBuilder builder, Object[] objectArray) {
        Account account = new Account();
        Integer id = (Integer) objectArray[0];
        String accountNumber = (String) objectArray[1];
        String name = (String) objectArray[2];
        //
        account.setId(id);
        account.setAccountNumber(accountNumber);
        account.setName(name);
        return account;
    }

    @Override
    public Account getExcelType(int rowIndex, List<Cell> cellList) {
        if (rowIndex == 0) {
            return null;
        }
        Account account = new Account();
        int id = DataConverter.getInteger(cellList.get(0));
        String accountNumber = cellList.get(1).getStringCellValue();
        String name = cellList.get(2).getStringCellValue();
        //
        account.setId(id);
        account.setAccountNumber(accountNumber);
        account.setName(name);
        return account;
    }

    @Override
    public Object[] getExcelRow(Account account) {
        Object[] cellData = new String[3];
        cellData[0] = account.getId();
        cellData[1] = account.getAccountNumber();
        cellData[2] = account.getName();
        return cellData;
    }
}
