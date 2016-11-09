package excel.accounting.forms;

import excel.accounting.dialog.AbstractDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Form Dialog
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public abstract class AbstractFormDialog extends AbstractDialog {
    private Map<String, ValueBinder<?>> valueBinderMap;

    public AbstractFormDialog() {
        valueBinderMap = new HashMap<>();
    }

    protected TextBinder textBinder(String name, String title) {
        TextBinder binder = new TextBinder(name, title);
        valueBinderMap.put(name, binder);
        return binder;
    }

    protected BigDecimalBinder bigDecimalField(String name, String title) {
        BigDecimalBinder binder = new BigDecimalBinder(name, title);
        valueBinderMap.put(name, binder);
        return binder;
    }

    protected IntegerBinder integerField(String name, String title) {
        IntegerBinder binder = new IntegerBinder(name, title);
        valueBinderMap.put(name, binder);
        return binder;
    }

    public boolean isFormObjectModified() {
        for (ValueBinder<?> binder : valueBinderMap.values()) {
            if (binder.isModified()) {
                return true;
            }
        }
        return false;
    }
}
