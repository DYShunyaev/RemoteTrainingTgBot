package d.shunyaev.RemoteTrainingTgBot;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class TelegramBotLoadTest extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://127.0.0.1:8092")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    String telegramUpdatePayload = """
        {
          "update_id": 10000,
          "message": {
            "message_id": 1,
            "from": { "id": #{userId}, "is_bot": false, "first_name": "User_#{userId}", "last_name": "LoadTestShunyaev" },
            "chat": { "id": #{userId}, "type": "private" },
            "date": 1610000000,
            "text": "/start"
          }
        }
        """;

    FeederBuilder<Object> userIdFeeder =
            listFeeder(java.util.stream.Stream.generate(() ->
                    java.util.Map.<String, Object>of("userId", (long) (Math.random() * 10000000))
            ).limit(100000).toList());

    ScenarioBuilder scn = scenario("Симуляция пользователя телеграм бота")
            .feed(userIdFeeder)
            .exec(http("Отправка команды /start")
                    .post("/test/test-webhook")
                    .body(StringBody(telegramUpdatePayload))
                    .check(status().is(200)));

    {
        int pauseSecond = 4;
        int countAtOnceUsers = 1;
        int maxUsers = 5000;
        setUp(
                scn.injectOpen(
                        nothingFor(pauseSecond),
                        atOnceUsers(countAtOnceUsers),
                        rampUsers(maxUsers).during(60)
                )
        ).protocols(httpProtocol);
    }
}
