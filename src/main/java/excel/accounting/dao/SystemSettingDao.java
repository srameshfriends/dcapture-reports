package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.SystemSetting;
import excel.accounting.shared.DataConverter;

import java.util.Arrays;
import java.util.List;

/**
 * System Setting Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class SystemSettingDao extends AbstractDao<SystemSetting> {
    @Override
    protected String getTableName() {
        return "system_setting";
    }

    private List<SystemSetting> findByCodeArray(String... codeArray) {
        QueryBuilder queryBuilder = selectBuilder(SystemSetting.class);
        queryBuilder.whereAndIn("code", codeArray);
        return fetchList(queryBuilder);
    }

    public List<SystemSetting> findByGroupCode(String groupCode) {
        QueryBuilder queryBuilder = selectBuilder(SystemSetting.class);
        queryBuilder.where("group_code", groupCode);
        return fetchList(queryBuilder);
    }

    public List<SystemSetting> getSystemUser() {
        final String pf = "su";
        String[] codes = {pf + "u1", pf + "p1", pf + "u2", pf + "p2", pf + "u3", pf + "p3"};
        return findByCodeArray(codes);
    }

    public List<SystemSetting> getDefaultSystemUser() {
        final String pf = "su";
        SystemSetting u1 = createSystemSetting(pf + "u1", pf + "u1", "Abi", true);
        SystemSetting p1 = createSystemSetting(pf + "p1", pf + "u1", DataConverter.encode("password"), false);
        SystemSetting u2 = createSystemSetting(pf + "u2", pf + "u2", "Bha", true);
        SystemSetting p2 = createSystemSetting(pf + "p2", pf + "u2", DataConverter.encode("password"), false);
        SystemSetting u3 = createSystemSetting(pf + "u3", pf + "u3", "Cho", true);
        SystemSetting p3 = createSystemSetting(pf + "p3", pf + "u3", DataConverter.encode("password"), false);
        return Arrays.asList(u1, p1, u2, p2, u3, p3);
    }

    private SystemSetting createSystemSetting(String code, String groupCode, String textValue, boolean boolValue) {
        SystemSetting systemSetting = new SystemSetting();
        systemSetting.setCode(code);
        systemSetting.setGroupCode(groupCode);
        systemSetting.setTextValue(textValue);
        systemSetting.setBoolValue(boolValue);
        return systemSetting;
    }
}
