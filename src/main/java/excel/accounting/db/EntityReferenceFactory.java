package excel.accounting.db;

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Table Reference Factory
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class EntityReferenceFactory {
    private static EntityReferenceFactory factory;



    public static EntityReferenceFactory instance() {
        if (factory == null) {
            factory = new EntityReferenceFactory();
        }
        return factory;
    }






}
