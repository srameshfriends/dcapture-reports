package excel.accounting.service;

import excel.accounting.db.DataProcessor;
import excel.accounting.db.QueryBuilder;
import excel.accounting.entity.Account;
import excel.accounting.model.RowData;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.RowDataProvider;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Account Service
 */
public class AccountService implements RowDataProvider<Account> {
    private DataProcessor dataProcessor;

    public void setDataProcessor(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public List<Account> loadAll() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.append("SELECT id, code, name FROM entity.account;");
        return dataProcessor.findRowDataList(queryBuilder, this);
    }

    public void insert(List<Account> accountList) {
        for (Account account : accountList) {
            dataProcessor.insert(account.updateQuery(null));
        }
    }

    @Override
    public Account getRowData(int rowIndex, List<Cell> cellList) {
        if (rowIndex == 0) {
            return null;
        }
        Account account = new Account();
        int id = DataConverter.getInteger(cellList.get(0));
        String code = cellList.get(1).getStringCellValue();
        String name = cellList.get(2).getStringCellValue();
        //
        account.setId(id);
        account.setCode(code);
        account.setName(name);
        return account;
    }

    @Override
    public Account getRowData(String queryName, Object[] objectArray) {
        Account account = new Account();
        Integer id = (Integer) objectArray[0];
        String code = (String) objectArray[1];
        String name = (String) objectArray[2];
        //
        account.setId(id);
        account.setCode(code);
        account.setName(name);
        return account;
    }

    @Override
    public String[] getCellData(Account account) {
        String[] cellData = new String[3];
        cellData[0] = account.getId() + "";
        cellData[1] = account.getCode();
        cellData[2] = account.getName();
        return cellData;
    }
}
