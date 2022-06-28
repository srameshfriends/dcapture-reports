package dcapture.reports.jasper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonJRDataSource implements JRDataSource {
    private static final Logger logger = LoggerFactory.getLogger(JsonJRDataSource.class);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd hh:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
    private final ObjectNode parameterNode;
    private final Map<String, String> parameterTypeMap, dataTypeMap;
    private final ArrayNode dataNode;
    private ObjectNode iteratorNode;
    private int iteratorIndex;
    private boolean isDebugMode;
    private StringBuilder debugBuilder;

    public JsonJRDataSource(ObjectNode dataObject, ObjectNode dataFormat) {
        ObjectNode pObj;
        ArrayNode dNode;
        JsonNode jsonData = dataObject.get("data"), jsonParam = dataObject.get("parameters");
        if (jsonData instanceof ArrayNode) {
            dNode = ((ArrayNode) jsonData);
        } else {
            dNode = new ObjectMapper().createArrayNode();
        }
        if (jsonParam instanceof ObjectNode) {
            pObj = (ObjectNode) jsonParam;
        } else {
            pObj = new ObjectMapper().createObjectNode();
        }
        parameterNode = pObj;
        dataNode = dNode;
        Map<String, String> paramTypeMp = new HashMap<>(), daTypeMap = new HashMap<>();
        JsonNode node1 = dataFormat.get("parameters"), node2 = dataFormat.get("data");
        if (node1 instanceof ObjectNode) {
            ObjectNode objNode1 = (ObjectNode) node1;
            objNode1.fieldNames().forEachRemaining(str -> {
                String type1 = objNode1.get(str).textValue();
                paramTypeMp.put(str, type1);
            });
        }
        if (node2 instanceof ObjectNode) {
            ObjectNode objNode2 = (ObjectNode) node2;
            objNode2.fieldNames().forEachRemaining(str -> {
                String type1 = objNode2.get(str).textValue();
                daTypeMap.put(str, type1);
            });
        }
        this.parameterTypeMap = Collections.unmodifiableMap(paramTypeMp);
        this.dataTypeMap = Collections.unmodifiableMap(daTypeMap);
        debugBuilder = new StringBuilder();
    }

    public JsonJRDataSource(HttpServletRequest request, ObjectNode dataFormat) {
        ObjectNode pObj = null;
        ArrayNode dNode = null;
        try (BufferedReader requestReader = request.getReader()) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.readValue(requestReader, ObjectNode.class);
            JsonNode jsonData = objectNode.get("data"), jsonParam = objectNode.get("parameters");
            if (jsonData instanceof ArrayNode) {
                dNode = ((ArrayNode) jsonData);
            } else {
                dNode = new ObjectMapper().createArrayNode();
            }
            if (jsonParam instanceof ObjectNode) {
                pObj = (ObjectNode) jsonParam;
            } else {
                pObj = new ObjectMapper().createObjectNode();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        parameterNode = pObj;
        dataNode = dNode;
        Map<String, String> paramTypeMp = new HashMap<>(), daTypeMap = new HashMap<>();
        JsonNode node1 = dataFormat.get("parameters"), node2 = dataFormat.get("data");
        if (node1 instanceof ObjectNode) {
            ObjectNode objNode1 = (ObjectNode) node1;
            objNode1.fieldNames().forEachRemaining(str -> {
                String type1 = objNode1.get(str).textValue();
                paramTypeMp.put(str, type1);
            });
        }
        if (node2 instanceof ObjectNode) {
            ObjectNode objNode2 = (ObjectNode) node2;
            objNode2.fieldNames().forEachRemaining(str -> {
                String type1 = objNode2.get(str).textValue();
                daTypeMap.put(str, type1);
            });
        }
        this.parameterTypeMap = Collections.unmodifiableMap(paramTypeMp);
        this.dataTypeMap = Collections.unmodifiableMap(daTypeMap);
        debugBuilder = new StringBuilder();
    }

    public void setDebugMode(boolean isDebugMode) {
        this.isDebugMode = isDebugMode;
    }

    public ArrayNode getData() {
        return dataNode;
    }

    public Map<String, Object> getParameters() {
        Map<String, Object> parameterMap = new HashMap<>();
        if (isDebugMode) {
            logger.info("\t PARAMETER");
            debugBuilder = new StringBuilder();
            parameterNode.fieldNames().forEachRemaining(name -> {
                Object paramValue = getTypeValue(false, name, parameterNode.get(name));
                parameterMap.put(name, paramValue);
                debugBuilder.append("\t").append(getParameterType(name)).append("\t").append(name).append("\t")
                        .append(paramValue).append("\n");
            });
            logger.info(debugBuilder.toString());
        } else {
            parameterNode.fieldNames().forEachRemaining(name -> parameterMap.put(name,
                    getTypeValue(false, name, parameterNode.get(name))));
        }
        return parameterMap;
    }

    private String getParameterType(String name) {
        return parameterTypeMap.getOrDefault(name, "string");
    }

    private String getDataType(String name) {
        return dataTypeMap.getOrDefault(name, "string");
    }

    private Object getTypeValue(boolean isDataType, String name, JsonNode node) {
        String type = isDataType ? getDataType(name) : getParameterType(name);
        if (node == null || node.isNull()) {
            return getDefaultValue(type);
        } else if ("string".equals(type)) {
            return node.textValue();
        } else if ("currency".equals(type)) {
            if (JsonNodeType.NUMBER.equals(node.getNodeType())) {
                return node.decimalValue();
            }
            return 0;
        } else if ("int".equals(type)) {
            return node.asInt();
        } else if ("decimal".equals(type)) {
            BigDecimal bigDecimal = node.decimalValue();
            return BigDecimal.ZERO.equals(bigDecimal) ? null : bigDecimal;
        } else if ("long".equals(type)) {
            return node.longValue();
        } else if ("number".equals(type)) {
            return node.doubleValue();
        } else if ("percentage".equals(type)) {
            return node.textValue() + "%";
        } else if ("date".equals(type)) {
            try {
                return dateFormat.parse(node.textValue());
            } catch (ParseException ee) {
                logger.error("Date Parse : " + ee.getMessage());
            }
            return null;
        } else if ("date_time".equals(type) || "time".equals(type)) {
            try {
                return timeFormat.parse(node.textValue());
            } catch (ParseException pe) {
                logger.error("Date Time Parse : " + pe.getMessage());
            }
            return null;
        }
        return node.toString();
    }

    private Object getDefaultValue(String type) {
        if (type == null || "string".equals(type)) {
            return null;
        } else if ("currency".equals(type) || "decimal".equals(type)) {
            return null;
        } else if ("int".equals(type)) {
            return 0;
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
            JsonNode jsonNode = dataNode.get(iteratorIndex);
            if (jsonNode instanceof ObjectNode) {
                iteratorNode = (ObjectNode) jsonNode;
                isNextRecord = true;
            } else {
                iteratorNode = null;
            }
        }
        iteratorIndex += 1;
        if (isDebugMode) {
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
        if (isDebugMode) {
            Object value = getTypeValue(true, jrField.getName(), iteratorNode.get(jrField.getName()));
            debugBuilder.append(getDataType(jrField.getName())).append("\t")
                    .append(jrField.getName()).append("\t").append(value).append("\n");
            return value;
        }
        return getTypeValue(true, jrField.getName(), iteratorNode.get(jrField.getName()));
    }
}
