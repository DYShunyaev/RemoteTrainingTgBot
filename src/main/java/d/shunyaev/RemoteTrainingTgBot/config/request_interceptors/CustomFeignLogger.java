package d.shunyaev.RemoteTrainingTgBot.config.request_interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class CustomFeignLogger extends feign.Logger {

    @Override
    protected void log(String configKey, String format, Object... args) {
        if (log.isDebugEnabled()) {
            log.info(format(configKey, format, args));
        }
    }

    private String format(String configKey, String format, Object... args) {
        String methodTagWithFormat = methodTag(configKey) + format;
        return methodTagWithFormat.formatted(args);
    }

    private void logHeaders(String configKey, Map<String, Collection<String>> headers) {
        for (String field : headers.keySet()) {

            for (String value : Util.valuesOrEmpty(headers, field)) {
                this.log(configKey, "%s: %s", field, value);
            }
        }
    }

    @Override
    protected void logRequest(String configKey, Logger.Level logLevel, Request request) {
        this.log(configKey, "---> %s %s HTTP/1.1", request.httpMethod()
                .name(), request.url());
        if (logLevel.ordinal() >= Logger.Level.HEADERS.ordinal()) {
            this.logHeaders(configKey, request.headers());

            int bodyLength = 0;
            if (request.body() != null) {
                bodyLength = request.body().length;
                if (logLevel.ordinal() >= Logger.Level.FULL.ordinal()) {
                    String bodyText = new String(request.body());
                    this.log(configKey, "");
                    if (bodyText != null) {
                        log(configKey, " REQUEST:\n%s", prettyPrintJsonToConsole(bodyText));
                    } else {
                        this.log(configKey, "%s", "Binary data");
                    }
                }
            }

            this.log(configKey, "---> END HTTP (%s-byte body)", bodyLength);
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Logger.Level logLevel, Response response,
                                              long elapsedTime) throws IOException {
        String reason = response.reason() != null && logLevel.compareTo(Logger.Level.NONE) > 0
                ? " " + response.reason()
                : "";
        int status = response.status();
        this.log(configKey, "<--- HTTP/1.1 %s%s (%sms)", status, reason, elapsedTime);
        if (logLevel.ordinal() >= Logger.Level.HEADERS.ordinal()) {
            this.logHeaders(configKey, response.headers());

            int bodyLength = 0;
            if (response.body() != null && status != 204 && status != 205) {
                if (logLevel.ordinal() >= Logger.Level.FULL.ordinal()) {
                    this.log(configKey, "");
                }

                byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                bodyLength = bodyData.length;
                if (logLevel.ordinal() >= Logger.Level.FULL.ordinal() && bodyLength > 0) {
                    log(configKey, " RESPONSE:\n%s",
                            prettyPrintJsonToConsole(Util.decodeOrDefault(bodyData, Util.UTF_8, "Binary data")));
                }

                this.log(configKey, "<--- END HTTP (%s-byte body)", bodyLength);
                return response.toBuilder().body(bodyData).build();
            }

            this.log(configKey, "<--- END HTTP (%s-byte body)", bodyLength);
        }

        return response;
    }

    @SneakyThrows
    private String prettyPrintJsonToConsole(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        Object jsonObject = mapper.readValue(jsonString, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
    }

}
