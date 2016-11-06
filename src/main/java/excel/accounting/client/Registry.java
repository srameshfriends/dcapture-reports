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
        manager.addView(new ChartOfAccountsView());
        manager.addView(new AccountView());
        // Income
        manager.addView(new IncomeCategoryView());
        manager.addView(new IncomeItemView());
        // Expense
        manager.addView(new ExpenseCategoryView());
        manager.addView(new ExpenseItemView());
        manager.addView(new PaymentView());
        // Assets
        manager.addView(new AssetView());
        // Management
        manager.addView(new BankTransactionView());
        manager.addView(new ExchangeRateView());
        manager.addView(new SystemSettingView());
    }

    static void registerBean(ApplicationControl control) {
        control.addBean("currencyService", new CurrencyService());
        control.addBean("accountService", new AccountService());
        control.addBean("incomeCategoryService", new IncomeCategoryService());
        control.addBean("incomeItemService", new IncomeItemService());
        control.addBean("expenseCategoryService", new ExpenseCategoryService());
        control.addBean("expenseItemService", new ExpenseItemService());
        control.addBean("bankTransactionService", new BankTransactionService());
        control.addBean("exchangeRateService", new ExchangeRateService());
        control.addBean("assetService", new AssetService());
        control.addBean("systemSettingService", new SystemSettingService());
        control.addBean("chartOfAccountsService", new ChartOfAccountsService());
        control.addBean("paymentService", new PaymentService());
        //
        control.addBean("currencyDao", new CurrencyDao());
        control.addBean("accountDao", new AccountDao());
        control.addBean("incomeItemDao", new IncomeItemDao());
        control.addBean("incomeCategoryDao", new IncomeCategoryDao());
        control.addBean("expenseCategoryDao", new ExpenseCategoryDao());
        control.addBean("expenseItemDao", new ExpenseItemDao());
        control.addBean("assetDao", new AssetDao());
        control.addBean("systemSettingDao", new SystemSettingDao());
        control.addBean("chartOfAccountsDao", new ChartOfAccountsDao());
        control.addBean("paymentDao", new PaymentDao());
    }
}
