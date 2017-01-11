package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.entity.*;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.ExpenseCategoryDao;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.RulesType;
import excel.accounting.shared.StringRules;
import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Expense Category Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public class ExpenseCategoryService extends AbstractService implements ExcelTypeConverter<ExpenseCategory> {
    private ExpenseCategoryDao expenseCategoryDao;

    private ExpenseCategoryDao getExpenseCategoryDao() {
        if (expenseCategoryDao == null) {
            expenseCategoryDao = (ExpenseCategoryDao) getBean("expenseCategoryDao");
        }
        return expenseCategoryDao;
    }

    @Override
    protected String getSqlFileName() {
        return "expense-category";
    }

    private boolean confirmValidate(ExpenseCategory expenseCategory) {
        return !(expenseCategory.getCode() == null || expenseCategory.getName() == null);
    }

    private boolean insertValidate(ExpenseCategory expenseCategory, StringRules rules) {
        return rules.isValid(expenseCategory.getCode()) && !StringRules.isEmpty(expenseCategory.getName());
    }

    private void updateStatus(List<ExpenseCategory> dataList) {
        /*QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory category : dataList) {
            transaction.addBatch(getColumnsMap("updateStatus", category));
        }
        executeBatch(transaction);*/
    }

    public void setAsDrafted(List<ExpenseCategory> dataList) {
        List<ExpenseCategory> filteredList = filteredByStatus(Status.Confirmed, dataList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : Only confirmed expense category set as drafted");
            return;
        }
        for (ExpenseCategory data : filteredList) {
            Object usedReference = getExpenseCategoryDao().getUsedReference(data);
            if (usedReference != null) {
                setMessage(usedReference.toString());
                return;
            }
            data.setStatus(Status.Drafted);
        }
        updateStatus(filteredList);
    }

    public void setAsConfirmed(List<ExpenseCategory> dataList) {
        List<ExpenseCategory> filteredList = filteredByStatus(Status.Drafted, dataList);
        if (filteredList.isEmpty()) {
            setMessage("Error : Only drafted expense category are allowed to confirm");
            return;
        }
        List<ExpenseCategory> validList = new ArrayList<>();
        filteredList.stream().filter(this::confirmValidate).forEach(account -> {
            account.setStatus(Status.Confirmed);
            validList.add(account);
        });
        if (validList.isEmpty()) {
            setMessage("Error : valid drafted expense category not found");
            return;
        }
        updateStatus(validList);
    }

    public void setAsClosed(List<ExpenseCategory> dataList) {
        List<ExpenseCategory> filteredList = filteredByStatus(Status.Confirmed, dataList);
        if (filteredList.isEmpty()) {
            setMessage("Error : Only confirmed expense category should be closed");
            return;
        }
        for (ExpenseCategory expenseCategory : filteredList) {
            expenseCategory.setStatus(Status.Closed);
        }
        updateStatus(filteredList);
    }

    public void reopenExpenseCategory(List<ExpenseCategory> dataList) {
        List<ExpenseCategory> filteredList = filteredByStatus(Status.Closed, dataList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : closed expense category are allowed to reopen");
            return;
        }
        for (ExpenseCategory expenseCategory : filteredList) {
            expenseCategory.setStatus(Status.Confirmed);
        }
        updateStatus(filteredList);
    }

    public void insertExpenseCategory(List<ExpenseCategory> dataList) {
        setMessage("Expense category, code, name should not be empty");
        StringRules rules = new StringRules();
        rules.setMinMaxLength(2, 6);
        rules.setFirstCharAlphaOnly(true);
        rules.setRulesType(RulesType.Alphanumeric);
        //
        List<String> existingList = new ArrayList<>(); // getExpenseCategoryDao().findCodeList();
        List<ExpenseCategory> validList = new ArrayList<>();
        for (ExpenseCategory expenseCategory : dataList) {
            if (insertValidate(expenseCategory, rules) && !existingList.contains(expenseCategory.getCode())) {
                validList.add(expenseCategory);
            }
        }
        if (validList.isEmpty()) {
            setMessage("Valid expense category not found");
            return;
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("insertExpenseCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory expenseCategory : validList) {
            transaction.addBatch(getColumnsMap("insertExpenseCategory", expenseCategory));
        }
        executeBatch(transaction);*/
    }

    public void updateExpenseCategory(List<ExpenseCategory> categoryList) {
        /*QueryBuilder queryBuilder = getQueryBuilder("updateExpenseCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory category : categoryList) {
            transaction.addBatch(getColumnsMap("updateExpenseCategory", category));
        }
        executeBatch(transaction);*/
    }

    public void deleteExpenseCategory(List<ExpenseCategory> categoryList) {
        List<ExpenseCategory> filteredList = filteredByStatus(Status.Drafted, categoryList);
        if (filteredList.isEmpty()) {
            return;
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("deleteExpenseCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory category : filteredList) {
            transaction.addBatch(getColumnsMap("deleteExpenseCategory", category));
        }
        executeBatch(transaction);*/
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Category Code", "Name", "Chart Of Accounts", "Description", "Status"};
    }

    @Override
    public ExpenseCategory getExcelType(String type, Cell[] array) {
        ExpenseCategory category = new ExpenseCategory();
        category.setCode(DataConverter.getString(array[0]));
        category.setName(DataConverter.getString(array[1]));
        category.setDescription(DataConverter.getString(array[3]));
        category.setStatus(DataConverter.getStatus(array[4]));
        return category;
    }

    @Override
    public Object[] getExcelRow(String type, ExpenseCategory category) {
        Object[] cellData = new Object[5];
        cellData[0] = category.getCode();
        cellData[1] = category.getName();
        cellData[3] = category.getDescription();
        cellData[4] = category.getStatus();
        return cellData;
    }

    private List<ExpenseCategory> filteredByStatus(Status status, List<ExpenseCategory> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
