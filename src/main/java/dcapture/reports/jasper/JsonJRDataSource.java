package dcapture.reports.jasper;

import dcapture.reports.util.CurrencyFormat;
import jakarta.json.*;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonJRDataSource implements JRDataSource {
    private static final Logger logger = LoggerFactory.getLogger(JsonJRDataSource.class);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final JRTypeMap typeMap;
    private final JsonObject parameters, config;
    private final JsonArray data;

    private CurrencyFormat currencyFormat;
    private JsonObject iteratorNode;
    private int iteratorIndex;
    private StringBuilder debugBuilder;

    public JsonJRDataSource(JRTypeMap jrTypeMap, JsonObject config, JsonObject param, JsonArray dataArray) {
        this.config = config;
        this.parameters = param;
        this.data = dataArray;
        this.typeMap = jrTypeMap;
        debugBuilder = new StringBuilder();
    }

    public JsonArray getData() {
        return data;
    }

    public Map<String, Object> getParameters() {
        Map<String, Object> parameterMap = new HashMap<>();
        if (logger.isDebugEnabled()) {
            logger.info("\t PARAMETER");
            debugBuilder = new StringBuilder();
            parameters.forEach((str, jsonValue) -> {
                Object paramValue = getTypeValue(false, str, jsonValue);
                parameterMap.put(str, paramValue);
                debugBuilder.append("\t").append(getParameterType(str)).append("\t").append(str).append("\t")
                        .append(paramValue).append("\n");
            });
            logger.info(debugBuilder.toString());
        } else {
            parameters.forEach((str, jsonValue) -> parameterMap.put(str,
                    getTypeValue(false, str, jsonValue)));
        }
        addConfigParam(parameterMap);
        return parameterMap;
    }

    private String getParameterType(String name) {
        return typeMap.getParameterTypeMap().getOrDefault(name, "string");
    }

    private String getDataType(String name) {
        return typeMap.getDataTypeMap().getOrDefault(name, "string");
    }

    private Object getTypeValue(boolean isDataType, String name, JsonValue json) {
        String type = isDataType ? getDataType(name) : getParameterType(name);
        if (json == null || json.getValueType().equals(JsonValue.ValueType.NULL)) {
            return getDefaultValue(type);
        }
        return switch (type) {
            case "string" -> asString(json);
            case "decimal" -> BigDecimal.valueOf(asNumber(json, type).doubleValue());
            case "long" -> asNumber(json, type).longValue();
            case "percentage", "double" -> asNumber(json, type).doubleValue();
            case "boolean" -> asBoolean(json);
            case "date" -> asDate(json);
            case "datetime" -> asDateTime(json);
            case "currency" -> asCurrency(json);
            case "int" -> asNumber(json, type).intValue();
            default -> throw new IllegalArgumentException("Data Type not yet implemented " + type + " \t " + json);
        };
    }

    private Number asNumber(JsonValue json, String type) {
        if (!(json instanceof JsonNumber jsonNum)) {
            return switch (type) {
                case "decimal", "currency" -> BigDecimal.ZERO;
                case "long" -> 0L;
                case "double", "percentage" -> 0D;
                default -> 0;
            };
        }
        return switch (type) {
            case "decimal", "currency" -> jsonNum.bigDecimalValue();
            case "long" -> jsonNum.longValue();
            case "double", "percentage" -> jsonNum.doubleValue();
            case "int" -> jsonNum.intValue();
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

    private String asCurrency(JsonValue json) {
        return getCurrencyFormat().format(asNumber(json, "currency").doubleValue());
    }

    private CurrencyFormat getCurrencyFormat() {
        if (currencyFormat == null) {
            currencyFormat = new CurrencyFormat();
            if (config.containsKey("currency")) {
                currencyFormat.setIndianCurrency("INR".equals(config.getString("currency")));
            }
            if (config.containsKey("precision")) {
                int precision = config.getInt("precision", 2);
                currencyFormat.setMathContext(new MathContext(precision));
            }
        }
        return currencyFormat;
    }

    private void addConfigParam(Map<String, Object> paramMap) {
        if (config.containsKey("org_logo")) {
            String suffix = config.getString("org_logo");
            URL logoUrl = getClassPathURL(suffix, "Organisation logo url not found at (" + suffix + ")");
            paramMap.put("org_logo", logoUrl);
        }
        if (config.containsKey("upi_pay_qr")) {
            String suffix = config.getString("upi_pay_qr");
            URL payQRUrl = getClassPathURL(suffix, "Organisation upi payment QR url not found at (" + suffix + ")");
            paramMap.put("upi_pay_qr", payQRUrl);
        }
        paramMap.put("printed_on", timeFormat.format(new Date()));
    }

    private URL getClassPathURL(String suffix, String error) {
        try {
            ClassPathResource resource = new ClassPathResource("static" + suffix);
            if (!resource.exists()) {
                throw new NullPointerException(error);
            }
            return resource.getURL();
        } catch (IOException ex) {
            throw new RuntimeException(error, ex);
        }
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
        } else if ("double".equals(type) || "percentage".equals(type)) {
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
        int size = data.size();
        boolean isNextRecord = false;
        if (0 < size && iteratorIndex < size) {
            JsonValue jsonNode = data.get(iteratorIndex);
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
