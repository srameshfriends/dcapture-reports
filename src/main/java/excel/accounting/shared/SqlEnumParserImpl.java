package excel.accounting.shared;

import excel.accounting.db.SqlEnumParser;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.Status;

/**
 * Orm Enum Parser Impl
 */
class SqlEnumParserImpl implements SqlEnumParser {

    @Override
    public Object getEnum(final Class<?> typeClass, final String value) {
        if (Status.class.equals(typeClass)) {
            return toEnum(Status.class, value, Status.Drafted);
        } else if (AccountType.class.equals(typeClass)) {
            return toEnum(AccountType.class, value, AccountType.Expense);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> E toEnum(Class<E> enumClass, String name, Object defaultValue) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (E)defaultValue;
    }
}
