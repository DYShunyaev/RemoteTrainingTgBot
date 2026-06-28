package d.shunyaev.RemoteTrainingTgBot.controller;

import d.shunyaev.RemoteTrainingTgBot.service.TelegramUpdateProcessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class TestLoadController {

    private final TelegramUpdateProcessor telegramUpdateProcessor;

    public TestLoadController(TelegramUpdateProcessor telegramUpdateProcessor) {
        this.telegramUpdateProcessor = telegramUpdateProcessor;
    }

    @PostMapping("/test/test-webhook")
    public void testWebhook(@RequestBody Update update) {
        telegramUpdateProcessor.processUpdate(
                update,
                msg -> {},
                msgList -> {}
        );
    }
}

