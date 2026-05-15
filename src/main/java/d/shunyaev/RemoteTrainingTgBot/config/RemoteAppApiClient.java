package d.shunyaev.RemoteTrainingTgBot.config;

import d.shunyaev.ApiClient;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.CustomErrorDecoder;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.CustomFeignLogger;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.TraceIdInterceptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import feign.Logger;
import feign.Retryer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@PropertySource("classpath:application.properties")
public class RemoteAppApiClient extends ApiClient {

    @Value("${app.url}")
    private String url;

    @Value("${app.user}")
    private String user;

    @Value("${app.pass}")
    private String pass;

    public RemoteAppApiClient() {
        super();
    }

    @PostConstruct
    public void initialize() {
        if (url == null || user == null || pass == null) {
            throw new IllegalStateException("Настройки app.url, app.user или app.pass не найдены!");
        }

        String credentials = "%s:%s".formatted(user, pass);
        String baseAuthCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        super.setBasePath(url);
        super.getFeignBuilder()
                .logger(new CustomFeignLogger())
                .logLevel(Logger.Level.FULL)
                .errorDecoder(new CustomErrorDecoder())
                .requestInterceptor(new TraceIdInterceptor())
                .requestInterceptor(requestTemplate ->
                        requestTemplate.header("Content-Type", "application/json"))
                .requestInterceptor(requestTemplate ->
                        requestTemplate.header("Authorization", "Basic %s".formatted(baseAuthCredentials)))
                .retryer(Retryer.NEVER_RETRY);
    }
}