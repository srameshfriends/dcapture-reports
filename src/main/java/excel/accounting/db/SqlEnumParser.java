package excel.accounting.db;

/**
 * Orm Enum Parser
 */
public interface SqlEnumParser {
    Object getEnum(final Class<?> typeClass, final String value);
}
