package excel.accounting.forms;

/**
 * Value Binder
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public interface ValueBinder<T> {
    String getFieldName();

    T getFieldValue();

    void setFieldValue(T value);

    boolean isModified();
}
