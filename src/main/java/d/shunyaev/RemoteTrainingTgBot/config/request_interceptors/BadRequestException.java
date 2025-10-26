package d.shunyaev.RemoteTrainingTgBot.config.request_interceptors;

import d.shunyaev.model.ResponseContainerResult;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import lombok.Getter;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class BadRequestException extends RuntimeException {

    @Getter
    private final ResponseContainerResult responseBody;

    public BadRequestException(byte[] responseBody) {
        this.responseBody = convertToResult(responseBody);
    }

    private ResponseContainerResult convertToResult(byte[] responseBody) {
        String body = new String(responseBody, StandardCharsets.UTF_8);
        JsonObject json;
        try (JsonReader jsonReader = Json.createReader(new StringReader(body))){
            json = jsonReader.readObject();
        }
        return new ResponseContainerResult()
                .code(json.getInt("code"))
                .message(json.getString("message"))
                .traceId(json.getString("trace_id"))
                .userInfo(json.getString("user_info"));
    }
}
