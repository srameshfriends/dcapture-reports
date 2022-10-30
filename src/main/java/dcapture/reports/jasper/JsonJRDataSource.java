package dcapture.reports.jasper;

import dcapture.reports.util.MessageException;
import jakarta.json.*;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonJRDataSource implements JRDataSource {
    private static final Logger logger = LoggerFactory.getLogger(JsonJRDataSource.class);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final JsonObject parameterNode;
    private final Map<String, String> parameterTypeMap, dataTypeMap;
    private final JsonArray dataNode;
    private JsonObject iteratorNode;
    private int iteratorIndex;
    private StringBuilder debugBuilder;

    public JsonJRDataSource(JsonObject dataObject, JsonObject dataFormat) {
        JsonObjectBuilder pObj;
        JsonArrayBuilder dNode;
        JsonObject jsonData = dataObject.getJsonObject("data"), jsonParam = dataObject.getJsonObject("parameters");
        if (jsonData instanceof JsonArray) {
            dNode = Json.createArrayBuilder((JsonArray) jsonData);
        } else {
            dNode = Json.createArrayBuilder();
        }
        if (jsonParam != null) {
            pObj = Json.createObjectBuilder(jsonParam);
        } else {
            pObj = Json.createObjectBuilder();
        }
        parameterNode = pObj.build();
        dataNode = dNode.build();
        Map<String, String> paramTypeMp = new HashMap<>(), daTypeMap = new HashMap<>();
        JsonObject node1 = dataFormat.getJsonObject("parameters"), node2 = dataFormat.getJsonObject("data");
        if (node1 != null) {
            node1.forEach((str, jsonValue) -> {
                if (jsonValue instanceof JsonString textJson) {
                    paramTypeMp.put(str, textJson.getString());
                }
            });
        }
        if (node2 != null) {
            node2.forEach((str, jsonValue) -> {
                if (jsonValue instanceof JsonString textJson) {
                    daTypeMap.put(str, textJson.getString());
                }
            });
        }
        this.parameterTypeMap = Collections.unmodifiableMap(paramTypeMp);
        this.dataTypeMap = Collections.unmodifiableMap(daTypeMap);
        debugBuilder = new StringBuilder();
    }

    public JsonJRDataSource(String jasperData, JsonObject dataFormat) {
        JsonObject paramObject = Json.createObjectBuilder().build();
        JsonArray dataArray = Json.createArrayBuilder().build();
        try (JsonReader parser = Json.createReader(new StringReader(jasperData))) {
            JsonObject objectNode = parser.readObject();
            for (Map.Entry<String, JsonValue> entity : objectNode.entrySet()) {
                if ("parameters".equalsIgnoreCase(entity.getKey())) {
                    if (entity.getValue() instanceof JsonObject) {
                        paramObject = entity.getValue().asJsonObject();
                    }
                } else if ("data".equalsIgnoreCase(entity.getKey())) {
                    if (entity.getValue() instanceof JsonArray) {
                        dataArray = entity.getValue().asJsonArray();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MessageException("jasper.data.error", ex.getMessage());
        }
        parameterNode = paramObject;
        dataNode = dataArray;
        Map<String, String> paramTypeMp = new HashMap<>(), daTypeMap = new HashMap<>();
        JsonValue node1 = dataFormat.get("parameters"), node2 = dataFormat.get("data");
        if (node1 instanceof JsonObject objNode1) {
            objNode1.forEach((str, jsonValue) -> {
                if (jsonValue instanceof JsonString jString) {
                    paramTypeMp.put(str, jString.getString());
                }
            });
        }
        if (node2 instanceof JsonObject objNode2) {
            objNode2.forEach((str, jsonValue) -> {
                if (jsonValue instanceof JsonString jString) {
                    daTypeMap.put(str, jString.getString());
                }
            });
        }
        this.parameterTypeMap = Collections.unmodifiableMap(paramTypeMp);
        this.dataTypeMap = Collections.unmodifiableMap(daTypeMap);
        debugBuilder = new StringBuilder();
    }

    public JsonArray getData() {
        return dataNode;
    }

    public Map<String, Object> getParameters() {
        Map<String, Object> parameterMap = new HashMap<>();
        if (logger.isDebugEnabled()) {
            logger.info("\t PARAMETER");
            debugBuilder = new StringBuilder();
            parameterNode.forEach((str, jsonValue) -> {
                Object paramValue = getTypeValue(false, str, jsonValue);
                parameterMap.put(str, paramValue);
                debugBuilder.append("\t").append(getParameterType(str)).append("\t").append(str).append("\t")
                        .append(paramValue).append("\n");
            });
            logger.info(debugBuilder.toString());
        } else {
            parameterNode.forEach((str, jsonValue) -> parameterMap.put(str,
                    getTypeValue(false, str, jsonValue)));
        }
        return parameterMap;
    }

    private String getParameterType(String name) {
        return parameterTypeMap.getOrDefault(name, "string");
    }

    private String getDataType(String name) {
        return dataTypeMap.getOrDefault(name, "string");
    }

    private Object getTypeValue(boolean isDataType, String name, JsonValue json) {
        String type = isDataType ? getDataType(name) : getParameterType(name);
        if (json == null || json.getValueType().equals(JsonValue.ValueType.NULL)) {
            return getDefaultValue(type);
        }
        return switch (type) {
            case "string" -> asString(json);
            case "currency", "decimal", "long", "double" -> asNumber(json, type);
            case "boolean" -> asBoolean(json);
            case "date" -> asDate(json);
            case "datetime" -> asDateTime(json);
            default -> json.toString();
        };
    }

    private Object asNumber(JsonValue json, String type) {
        if (!(json instanceof JsonNumber jsonNum)) {
            return switch (type) {
                case "currency", "decimal" -> BigDecimal.ZERO;
                case "long" -> 0L;
                case "double" -> 0D;
                default -> 0;
            };
        }
        return switch (type) {
            case "currency", "decimal" -> jsonNum.bigDecimalValue();
            case "long" -> jsonNum.longValue();
            case "double" -> jsonNum.doubleValue();
            default -> 0;
        };
    }

    private String asString(JsonValue json) {
        if (json == null || json.getValueType().equals(JsonValue.ValueType.NULL)) {
            return null;
        }
        if (json instanceof JsonString jsonString) {
            return jsonString.getString().trim();
        }
        return json.toString();
    }

    private boolean asBoolean(JsonValue json) {
        return json != null && json.getValueType().equals(JsonValue.ValueType.TRUE);
    }

    private Date asDate(JsonValue json) {
        String dateText = asString(json);
        if (dateText == null || 9 > dateText.length()) {
            return null;
        }
        try {
            return dateFormat.parse(dateText);
        } catch (ParseException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            } else {
                logger.info("Date Parse Error : " + ex.getMessage());
            }
        }
        return null;
    }

    private Date asDateTime(JsonValue json) {
        String dateText = asString(json);
        if (dateText == null || 14 > dateText.length()) {
            return null;
        }
        try {
            return timeFormat.parse(dateText);
        } catch (ParseException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            } else {
                logger.info("Date Parse Error : " + ex.getMessage());
            }
        }
        return null;
    }

    private Object getDefaultValue(String type) {
        if (type == null || "string".equals(type)) {
            return null;
        } else if ("currency".equals(type) || "decimal".equals(type)) {
            return BigDecimal.ZERO;
        } else if ("int".equals(type)) {
            return 0;
        } else if ("double".equals(type)) {
            return 0D;
        } else if ("boolean".equals(type)) {
            return false;
        } else if ("number".equals(type)) {
            return 0.0;
        } else if ("long".equals(type)) {
            return 0L;
        }
        return null;
    }

    @Override
    public boolean next() {
        int size = dataNode.size();
        boolean isNextRecord = false;
        if (0 < size && iteratorIndex < size) {
            JsonValue jsonNode = dataNode.get(iteratorIndex);
            if (jsonNode instanceof JsonObject) {
                iteratorNode = (JsonObject) jsonNode;
                isNextRecord = true;
            } else {
                iteratorNode = null;
            }
        }
        iteratorIndex += 1;
        if (logger.isDebugEnabled()) {
            logger.info(debugBuilder.toString());
            debugBuilder = new StringBuilder("\t").append(iteratorIndex).append("\n");
        }
        return isNextRecord;
    }

    @Override
    public Object getFieldValue(JRField jrField) {
        if (iteratorNode == null) {
            throw new RuntimeException("Json Jasper Report row should not be empty.");
        }
        if (logger.isDebugEnabled()) {
            Object value = getTypeValue(true, jrField.getName(), iteratorNode.get(jrField.getName()));
            debugBuilder.append(getDataType(jrField.getName())).append("\t")
                    .append(jrField.getName()).append("\t").append(value).append("\n");
            return value;
        }
        return getTypeValue(true, jrField.getName(), iteratorNode.get(jrField.getName()));
    }
}
