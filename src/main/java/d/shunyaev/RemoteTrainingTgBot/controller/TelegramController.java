package d.shunyaev.RemoteTrainingTgBot.controller;

import d.shunyaev.RemoteTrainingTgBot.components.services.CreateExerciseComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.CreateTrainingComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.RegistrationComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.CreateUserComponent;
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

    private final RegistrationComponent registrationComponent;
    private final CreateUserComponent createUserComponent;
    private final CreateTrainingComponent createTrainingComponent;
    private final CreateExerciseComponent createExerciseComponent;
    private final Map<Long, SendMessage> beforeMessages = new HashMap<>();
    private SendMessage responseMessage = new SendMessage();

    public TelegramController(
            CreateUserComponent telegramService,
            RegistrationComponent registrationComponent,
            CreateTrainingComponent createTrainingComponent,
            CreateExerciseComponent createExerciseComponent
    ) {
        this.createUserComponent = telegramService;
        this.registrationComponent = registrationComponent;
        this.createTrainingComponent = createTrainingComponent;
        this.createExerciseComponent = createExerciseComponent;
    }

    public SendMessage createController(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message requestMessage = update.getMessage();

        long chatId = requestMessage != null ? requestMessage.getChatId() : callbackQuery.getFrom().getId();

        registrationController(requestMessage);
        createUserController(callbackQuery, requestMessage, chatId);
        createTrainingController(callbackQuery, requestMessage, chatId);
        createExerciseController(callbackQuery, requestMessage, chatId);

        beforeMessages.put(chatId, responseMessage);
        return responseMessage;
    }

    private void registrationController(Message requestMessage) {
        if (Objects.nonNull(requestMessage) && requestMessage.hasText() && "/start".equals(requestMessage.getText())) {
            responseMessage = registrationComponent.registration(requestMessage);
        }
    }

    private void createUserController(CallbackQuery callbackQuery, Message message, long chatId) {
        SendMessage beforeMessage = beforeMessages.get(chatId);
        String data = (callbackQuery != null) ? callbackQuery.getData() : "";
        String textCommand = data.replaceAll("/.*", "");

        if ("createUser".equals(textCommand)) {
            responseMessage = createUserComponent.createUser(callbackQuery, chatId);
            return;
        }

        if (beforeMessage == null) return;
        if (!data.contains(CREATE_USER.getUrl()) && Objects.nonNull(callbackQuery)) {
            return;
        }

        if (callbackQuery == null) {
            callbackQuery = createNewCallbackQuery(message);
        }

        String userInput = message != null ? message.getText() : null;
        String beforeText = beforeMessage.getText();

        if (beforeText.contains("email")) {
            callbackQuery.setData(CREATE_USER.getUrl() + EMAIL.getUrl() + userInput);
        } else if (beforeText.contains("вес") || beforeText.contains("Вес")) {
            callbackQuery.setData(CREATE_USER.getUrl() + WEIGHT.getUrl() + userInput);
        } else if (beforeText.contains("рост") || beforeText.contains("Рост")) {
            callbackQuery.setData(CREATE_USER.getUrl() + HEIGHT.getUrl() + userInput);
        } else if (beforeText.contains("дату рождения")) {
            callbackQuery.setData(CREATE_USER.getUrl() + DATE_OF_BIRTH.getUrl() + userInput);
        } else if (beforeText.contains("Фамилию")) {
            callbackQuery.setData(CREATE_USER.getUrl() + LAST_NAME.getUrl() + userInput);
        }

        responseMessage = createUserComponent.createUser(callbackQuery, chatId);
    }

    private void createTrainingController(CallbackQuery callbackQuery, Message message, long chatId) {
        String data = (callbackQuery != null) ? callbackQuery.getData() : "";
        String textCommand = data.replaceAll("/.*", "");
        String textMessage = Objects.nonNull(message)
                ? message.getText()
                : "";

        if ("createNewTraining".equals(textCommand) || textMessage.equals("/create_new_training")) {
            responseMessage = createTrainingComponent.createTraining(callbackQuery, chatId);
        }
    }

    private void createExerciseController(CallbackQuery callbackQuery, Message message, long chatId) {
        String data = (callbackQuery != null) ? callbackQuery.getData() : "";
        String textCommand = data.replaceAll("/.*", "");

        if (data.contains("done")) return;
        if ("createNewExercise".equals(textCommand)) {
            responseMessage = createExerciseComponent.createExercise(callbackQuery, chatId);
        }
    }

    private CallbackQuery createNewCallbackQuery(Message message) {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        return callbackQuery;
    }
}