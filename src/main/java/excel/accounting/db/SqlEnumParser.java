package excel.accounting.db;

/**
 * Orm Enum Parser
 */
public interface SqlEnumParser {
    <E> Object parseEnum(final Class<?> typeClass, final String value);
}
