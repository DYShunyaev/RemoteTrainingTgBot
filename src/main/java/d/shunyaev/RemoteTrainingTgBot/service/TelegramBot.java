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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final TelegramController telegramController;


    public TelegramBot(BotConfig config, TelegramController telegramController) {
        this.config = config;
        this.telegramController = telegramController;
        List<BotCommand> listOfCommands = new ArrayList<>();

        listOfCommands.add(new BotCommand("/start", "Начать работу"));
        listOfCommands.add(new BotCommand("/create_new_training", "Создать новую тренировку"));
        listOfCommands.add(new BotCommand("/get_my_trainings", "Мои тренировки"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (Objects.nonNull(update.getMessage()) && update.getMessage().hasText()
                && update.getMessage().getText().contains("get")) {
            sendMessages(telegramController.getController(update));
        } else if (Objects.nonNull(update.getCallbackQuery()) && Objects.nonNull(update.getCallbackQuery().getData())
                && update.getCallbackQuery().getData().contains("editMessage")) {
            sendMessage(telegramController.editMessageController(update));
        }
        else {
            sendMessage(telegramController.createController(update));
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    public void sendMessages(List<SendMessage> messageList) {
        messageList.forEach(this::sendMessage);
    }

    public void sendMessage(SendMessage message) {
        try {
            if (Objects.nonNull(message.getChatId())) {
                execute(message);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public void sendMessage(EditMessageText message) {
        try {
            if (Objects.nonNull(message.getChatId())) {
                execute(message);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

}
