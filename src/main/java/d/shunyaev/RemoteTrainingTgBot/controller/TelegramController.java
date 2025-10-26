package d.shunyaev.RemoteTrainingTgBot.controller;

import d.shunyaev.RemoteTrainingTgBot.components.RegistrationComponent;
import d.shunyaev.RemoteTrainingTgBot.components.CreateUserComponent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.*;

@Component
public class TelegramController {

    private final CreateUserComponent telegramService;
    private final RegistrationComponent registrationComponent;
    private final Map<Long, SendMessage> BEFORE_MESSAGES = new HashMap<>();
    private SendMessage responseMessage = new SendMessage();

    public TelegramController(CreateUserComponent telegramService, RegistrationComponent registrationComponent) {
        this.telegramService = telegramService;
        this.registrationComponent = registrationComponent;
    }

    public SendMessage mainController(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message requestMessage = update.getMessage();

        long chatId = requestMessage != null
                ? requestMessage.getChatId() : callbackQuery.getFrom().getId();
        registrationController(requestMessage);
        createUserController(callbackQuery, requestMessage, chatId);
        BEFORE_MESSAGES.put(chatId, responseMessage);
        return responseMessage;
    }

    private void registrationController(Message requestMessage) {
        if (Objects.nonNull(requestMessage)) {
            if (requestMessage.hasText()) {
                if (requestMessage.getText().equals("/start")) {
                    responseMessage = registrationComponent.registration(requestMessage);
                }
            }
        }
    }

    private void createUserController(CallbackQuery callbackQuery, Message message, long chatId) {
        SendMessage beforeMessage = BEFORE_MESSAGES.get(chatId);
        String textMessage = Objects.nonNull(callbackQuery)
                ? callbackQuery.getData().replaceAll("/.*", "")
                : "";
        if (textMessage.equals("createUser")) {
            responseMessage = telegramService.createUser(callbackQuery, chatId);
        } else if (Objects.nonNull(beforeMessage)) {
            if (Objects.isNull(callbackQuery)) {
                callbackQuery = createNewCallbackQuery(message);
            }
            if (beforeMessage.getText().contains("email")) {
                callbackQuery.setData(CREATE_USER.getUrl() + EMAIL.getUrl() + message.getText());
            } else if (beforeMessage.getText().contains("вес") || beforeMessage.getText().contains("Вес")) {
                callbackQuery.setData(CREATE_USER.getUrl() + WEIGHT.getUrl() + message.getText());
            } else if (beforeMessage.getText().contains("рост") || beforeMessage.getText().contains("Рост")) {
                callbackQuery.setData(CREATE_USER.getUrl() + HEIGHT.getUrl() + message.getText());
            } else if (beforeMessage.getText().contains("дату рождения")) {
                callbackQuery.setData(CREATE_USER.getUrl() + DATE_OF_BIRTH.getUrl() + message.getText());
            } else if (beforeMessage.getText().contains("Фамилию")) {
                callbackQuery.setData(CREATE_USER.getUrl() + LAST_NAME.getUrl() + message.getText());
            }
            responseMessage = telegramService.createUser(callbackQuery, chatId);
        }
    }

    private CallbackQuery createNewCallbackQuery(Message message) {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        return callbackQuery;
    }
}