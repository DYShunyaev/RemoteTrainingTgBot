package d.shunyaev.RemoteTrainingTgBot.config;

import d.shunyaev.ApiClient;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.CustomErrorDecoder;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.CustomFeignLogger;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.TraceIdInterceptor;
import feign.Logger;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RemoteAppApiClient extends ApiClient {

    @Value("${app.url}")
    private String url;
    @Value("${app.user}")
    private String user;
    @Value("${app.pass}")
    private String pass;

    public RemoteAppApiClient() {
        this("http://localhost:8081", Base64.getEncoder().encodeToString(
                "%s:%s".formatted("user", "pass").getBytes(StandardCharsets.UTF_8))
        );
    }

    public RemoteAppApiClient(String url, String baseAuthCredentials) {
        super.setBasePath(url);
        super.getFeignBuilder()
                .logger(new CustomFeignLogger())
                .logLevel(Logger.Level.FULL)
                .errorDecoder(new CustomErrorDecoder())
                .requestInterceptor(new TraceIdInterceptor())
                .requestInterceptor(requestTemplate ->
                        requestTemplate.header("Content-Type", new String[]{"application/json"}))
                .requestInterceptor(requestTemplate ->
                        requestTemplate.header("Authorization",
                                new String[]{"Basic %s".formatted(baseAuthCredentials)}))
                .retryer(Retryer.NEVER_RETRY);
    }
}



//package d.shunyaev.RemoteTrainingTgBot.config;
//
//        import d.shunyaev.ApiClient;
//        import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.TraceIdInterceptor;
//        import feign.Logger;
//        import feign.Retryer;
//        import feign.codec.ErrorDecoder;
//        import org.springframework.beans.factory.annotation.Value;
//        import org.springframework.stereotype.Component;
//
//        import javax.annotation.PostConstruct;
//        import java.nio.charset.StandardCharsets;
//        import java.util.Base64;
//
//@Component
//public class RemoteAppApiClient extends ApiClient {
//
//    @Value("${app.url}")
//    private String url;
//
//    @Value("${app.user}")
//    private String user;
//
//    @Value("${app.pass}")
//    private String pass;
//
//    @PostConstruct
//    public void init() {
//        initializeApiClient();
//    }
//
//    private void initializeApiClient() {
//        super.setBasePath(url);
//        super.getFeignBuilder()
//                .logger(new Logger.JavaLogger())
//                .logLevel(Logger.Level.FULL)
//                .errorDecoder(new ErrorDecoder.Default())
//                .requestInterceptor(new TraceIdInterceptor())
//                .requestInterceptor(requestTemplate ->
//                        requestTemplate.header("Content-Type", "application/json"))
//                .requestInterceptor(requestTemplate ->
//                        requestTemplate.header("Authorization",
//                                "Basic %s".formatted(Base64.getEncoder().encodeToString(
//                                        "%s:%s".formatted(user, pass).getBytes(StandardCharsets.UTF_8)
//                                ))))
//                .retryer(Retryer.NEVER_RETRY);
//    }
//}