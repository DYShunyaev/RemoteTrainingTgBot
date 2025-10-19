package d.shunyaev.RemoteTrainingTgBot.config;

import d.shunyaev.ApiClient;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.TraceIdInterceptor;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RemoteAppApiClient extends ApiClient {

    public RemoteAppApiClient() {
        this("http://localhost:8081", Base64.getEncoder().encodeToString(
                "%s:%s".formatted("user", "pass").getBytes(StandardCharsets.UTF_8))
        );
    }

    public RemoteAppApiClient(String url, String baseAuthCredentials) {
        super.setBasePath(url);
        super.getFeignBuilder()
                .logger(new Logger.JavaLogger())
                .logLevel(Logger.Level.FULL)
                .errorDecoder(new ErrorDecoder.Default())
                .requestInterceptor(new TraceIdInterceptor())
                .requestInterceptor(requestTemplate ->
                        requestTemplate.header("Content-Type", new String[]{"application/json"}))
                .requestInterceptor(requestTemplate ->
                        requestTemplate.header("Authorization",
                                new String[]{"Basic %s".formatted(baseAuthCredentials)}))
                .retryer(Retryer.NEVER_RETRY);
    }
}
