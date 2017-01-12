package excel.accounting.db;

import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Abstract Transaction
 */
class AbstractTransaction {

    Object getFieldObject(Object obj, String fieldName) {
        try {
            return PropertyUtils.getProperty(obj, fieldName);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore Exception
        }
        return null;
    }
}
