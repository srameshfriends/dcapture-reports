package dcapture.reports.jasper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
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
    }

    public ArrayNode getData() {
        return dataNode;
    }

    public Map<String, Object> getParameters() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterNode.fieldNames().forEachRemaining(name -> parameterMap.put(name,
                getTypeValue(false, name, parameterNode.get(name))));
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
        if ("string".equals(type)) {
            return node.textValue();
        } else if ("currency".equals(type)) {
            if (node instanceof DecimalNode) {
                return node.decimalValue();
            } else if (node instanceof NumericNode) {
                return node.numberValue();
            }
            return node.asDouble();
        } else if ("int".equals(type)) {
            return node.asInt();
        } else if ("decimal".equals(type) || "number".equals(type)) {
            return node.asDouble();
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
        return isNextRecord;
    }

    @Override
    public Object getFieldValue(JRField jrField) {
        if (iteratorNode == null) {
            throw new RuntimeException("Json Jasper Report row should not be empty.");
        }
        return getTypeValue(true, jrField.getName(), iteratorNode.get(jrField.getName()));
    }
}
