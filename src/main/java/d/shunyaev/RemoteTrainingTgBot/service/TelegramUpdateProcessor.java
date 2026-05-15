package d.shunyaev.RemoteTrainingTgBot.service;

import d.shunyaev.RemoteTrainingTgBot.controller.TelegramController;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class TelegramUpdateProcessor {
    private final TelegramController telegramController;

    @Async
    public void processUpdate(Update update, Consumer<BotApiMethod<?>> messageSender, Consumer<List<SendMessage>> messagesSender) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().contains("get")) {
            messagesSender.accept(telegramController.getController(update));
            return;
        }

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data != null && !data.contains("create")
                    || data != null && data.contains("createNewTraining")
                    || data != null && data.contains("createNewExercise")
                    || data != null && data.contains("generateNewTraining")) {
                if (data.contains("editMessage")
                        || data.contains("update") || data.contains("createNewTraining")
                        || data.contains("generateNewTraining") || data.contains("createNewExercise")) {
                    messageSender.accept(telegramController.editMessageController(update));
                } else if ("back".equals(data)) {
                    messageSender.accept(telegramController.backMessage(update));
                }
                return;
            }
        }

        messageSender.accept(telegramController.createController(update));
    }
}