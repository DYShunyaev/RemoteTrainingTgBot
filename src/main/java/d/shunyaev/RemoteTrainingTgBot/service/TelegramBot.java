package d.shunyaev.RemoteTrainingTgBot.service;

import d.shunyaev.RemoteTrainingTgBot.config.BotConfig;
import d.shunyaev.RemoteTrainingTgBot.controller.TelegramController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final TelegramController telegramController;

    public TelegramBot(BotConfig config, TelegramController telegramController) {
        this.config = config;
        this.telegramController = telegramController;

        var commands = List.of(
                new BotCommand("/start", "Начать работу"),
                new BotCommand("/create_new_training", "Создать новую тренировку"),
                new BotCommand("/set_my_trainer", "Добавить тренера"),
                new BotCommand("/get_my_trainings", "Мои тренировки"),
                new BotCommand("/get_my_trainer", "Мой тренер"),
                new BotCommand("/get_my_data", "Мои данные")
        );

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting commands: {}", e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().contains("get")) {
            sendMessages(telegramController.getController(update));
            return;
        }

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (
                    data != null && !data.contains("create")
                    || data != null && data.contains("createNewTraining")
                    || data != null && data.contains("createNewExercise")
                    || data != null && data.contains("generateNewTraining")
            ) {
                if (data.contains("editMessage")
                        || data.contains("update") || data.contains("createNewTraining")
                        || data.contains("generateNewTraining") || data.contains("createNewExercise")) {
                    sendMessage(telegramController.editMessageController(update));
                } else if ("back".equals(data)) {
                    sendMessage(telegramController.backMessage(update));
                }
                return;
            }
        }

        sendMessage(telegramController.createController(update));
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    public void sendMessages(List<SendMessage> messages) {
        if (messages != null) messages.forEach(this::sendMessage);
    }

    public void sendMessage(Object message) {
        try {
            if (message instanceof SendMessage m && m.getChatId() != null) execute(m);
            if (message instanceof EditMessageText m && m.getChatId() != null) execute(m);
        } catch (TelegramApiException e) {
            log.error("Telegram error: {}", e.getMessage());
        }
    }
}
