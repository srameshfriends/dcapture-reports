package excel.accounting.client;

import excel.accounting.dao.*;
import excel.accounting.service.*;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.ui.ViewManager;
import excel.accounting.view.*;

/**
 * Registry
 *
 * @author Ramesh
 * @since Oct, 2016
 */
abstract class Registry {

    static void registerView(ViewManager manager) {
        // Register
        manager.addView(new CurrencyView());
        manager.addView(new AccountView());
        // Income
        manager.addView(new IncomeCategoryView());
        manager.addView(new IncomeItemView());
        // Expense
        manager.addView(new ExpenseCategoryView());
        manager.addView(new ExpenseItemView());
        // Assets
        manager.addView(new AssetView());
        // Management
        manager.addView(new BankTransactionView());
        manager.addView(new ExchangeRateView());
    }

    static void registerService(ApplicationControl control) {
        control.addService("currencyService", new CurrencyService());
        control.addService("accountService", new AccountService());
        control.addService("incomeCategoryService", new IncomeCategoryService());
        control.addService("incomeItemService", new IncomeItemService());
        control.addService("expenseCategoryService", new ExpenseCategoryService());
        control.addService("expenseItemService", new ExpenseItemService());
        control.addService("bankTransactionService", new BankTransactionService());
        control.addService("exchangeRateService", new ExchangeRateService());
        control.addService("assetService", new AssetService());
    }

    static void registerDao(ApplicationControl control) {
        control.addDao("currencyDao", new CurrencyDao());
        control.addDao("accountDao", new AccountDao());
        control.addDao("incomeItemDao", new IncomeItemDao());
        control.addDao("incomeCategoryDao", new IncomeCategoryDao());
        control.addDao("expenseCategoryDao", new ExpenseCategoryDao());
        control.addDao("expenseItemDao", new ExpenseItemDao());
        control.addDao("assetDao", new AssetDao());
    }
}
