package excel.accounting.client;

import excel.accounting.service.AccountService;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.shared.ViewManager;
import excel.accounting.view.AccountView;

/**
 * Registry
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public abstract class Registry {

    static void registerView(ViewManager manager) {
        manager.addView(new AccountView());
    }

    static void registerService(ApplicationControl control) {
        control.addService("accountService", new AccountService());

    }
}
