package excel.accounting.shared;

import excel.accounting.db.OrmEnumParser;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.Status;

/**
 * Orm Enum Parser Impl
 */
class OrmEnumParserImpl implements OrmEnumParser {

    @Override
    public Object getEnum(final Class<?> typeClass, final String value) {
        if (Status.class.equals(typeClass)) {
            return toEnum(Status.class, value);
        } else if (AccountType.class.equals(typeClass)) {
            return toEnum(AccountType.class, value);
        }
        return null;
    }

    private <E extends Enum<E>> E toEnum(Class<E> enumClass, String name) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
