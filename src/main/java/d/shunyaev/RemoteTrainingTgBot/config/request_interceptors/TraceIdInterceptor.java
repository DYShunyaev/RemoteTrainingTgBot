package d.shunyaev.RemoteTrainingTgBot.config.request_interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.UUID;

public class TraceIdInterceptor implements RequestInterceptor {
    @Override
    @SneakyThrows
    public void apply(RequestTemplate requestTemplate) {
        var mapper = new ObjectMapper();
        var typeReference = new TypeReference<HashMap<String, Object>>() {

        };

        HashMap<String, Object> bodyHashMap = mapper.readValue(requestTemplate.requestBody().asString(), typeReference);
        bodyHashMap.put("trace_id", UUID.randomUUID().toString());
        bodyHashMap.put("user_info", "telegram_bot");

        var bodyJson = mapper.writeValueAsString(bodyHashMap);
        Request.Body newBody = Request.Body.encoded(bodyJson.getBytes(), requestTemplate.requestCharset());
        requestTemplate.body(newBody);
    }
}
