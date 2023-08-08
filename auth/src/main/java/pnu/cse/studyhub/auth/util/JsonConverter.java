package pnu.cse.studyhub.auth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jose.shaded.json.parser.JSONParser;

public class JsonConverter {
    private final ObjectMapper objectMapper;

    public JsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    public JSONObject stringToJson(String strJson) {
        try {
            JSONParser jsonParser = new JSONParser();

            // To Object
            Object obj = jsonParser.parse(strJson);

            // To JsonObject
            JSONObject jsonObj = (JSONObject) obj;
            return jsonObj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
    public <T> T convertFromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

}
