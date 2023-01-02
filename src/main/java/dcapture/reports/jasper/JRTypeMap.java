package dcapture.reports.jasper;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JRTypeMap {
    private Map<String, String> parameterTypeMap, dataTypeMap, configTypeMap;

    private static Map<String, String> getTypeMap(JsonValue json) {
        Map<String, String> typeMap = new HashMap<>();
        if (json instanceof JsonObject obj) {
            obj.forEach((str, jsonValue) -> {
                if (jsonValue instanceof JsonString textJson) {
                    typeMap.put(str, textJson.getString());
                }
            });
        }
        return typeMap;
    }

    public Map<String, String> getParameterTypeMap() {
        return parameterTypeMap;
    }

    public void setParameterTypeMap(JsonValue json) {
        setParameterTypeMap(getTypeMap(json));
    }

    public void setParameterTypeMap(Map<String, String> parameterTypeMap) {
        this.parameterTypeMap = Collections.unmodifiableMap(parameterTypeMap);
    }

    public Map<String, String> getDataTypeMap() {
        return dataTypeMap;
    }

    public void setDataTypeMap(JsonValue json) {
        setDataTypeMap(getTypeMap(json));
    }

    public void setDataTypeMap(Map<String, String> dataTypeMap) {
        this.dataTypeMap = Collections.unmodifiableMap(dataTypeMap);
    }

    public Map<String, String> getConfigTypeMap() {
        return configTypeMap;
    }

    public void setConfigTypeMap(JsonValue json) {
        setConfigTypeMap(getTypeMap(json));
    }

    public void setConfigTypeMap(Map<String, String> configTypeMap) {
        this.configTypeMap = Collections.unmodifiableMap(configTypeMap);
    }
}
