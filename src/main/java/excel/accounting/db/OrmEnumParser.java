package excel.accounting.db;

/**
 * Orm Enum Parser
 */
public interface OrmEnumParser {
    Object getEnum(final Class<?> typeClass, final String value);
}
