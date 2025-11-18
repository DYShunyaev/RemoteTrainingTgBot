package d.shunyaev.RemoteTrainingTgBot.controller;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.*;
import d.shunyaev.RemoteTrainingTgBot.models.MessageCash;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.*;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.*;

@Component
public class TelegramController {

    private final RegistrationComponent registrationComponent;
    private final CreateUserComponent createUserComponent;
    private final CreateTrainingComponent createTrainingComponent;
    private final CreateExerciseComponent createExerciseComponent;
    private final GetTrainingsComponent getTrainingsComponent;
    private final UpdateTrainingsComponent updateTrainingsComponent;
    private final Map<Long, SendMessage> beforeMessages = new HashMap<>();
    private final Map<Long, EditMessageText> beforeEditMessages = new HashMap<>();

    public TelegramController(
            CreateUserComponent telegramService,
            RegistrationComponent registrationComponent,
            CreateTrainingComponent createTrainingComponent,
            CreateExerciseComponent createExerciseComponent,
            GetTrainingsComponent getTrainingsComponent,
            UpdateTrainingsComponent updateTrainingsComponent) {
        this.createUserComponent = telegramService;
        this.registrationComponent = registrationComponent;
        this.createTrainingComponent = createTrainingComponent;
        this.createExerciseComponent = createExerciseComponent;
        this.getTrainingsComponent = getTrainingsComponent;
        this.updateTrainingsComponent = updateTrainingsComponent;
    }

    public EditMessageText backMessage(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message requestMessage = update.getMessage();

        long chatId = requestMessage != null ? requestMessage.getChatId() : callbackQuery.getFrom().getId();
        EditMessageText editMessageText = getLastMessageByChatId(chatId, (Message) callbackQuery.getMessage());
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        return editMessageText;
    }

    public EditMessageText editMessageController(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message requestMessage = update.getMessage();

        long chatId = requestMessage != null ? requestMessage.getChatId() : callbackQuery.getFrom().getId();

        setMessageCash(chatId, (Message) callbackQuery.getMessage());

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setChatId(chatId);

        editMessageText = getTrainingsComponent.setTrainingIsDone(callbackQuery, chatId, editMessageText);
        editMessageText = updateTrainingsComponent.updateTraining(callbackQuery, chatId, editMessageText);

        beforeEditMessages.put(chatId, editMessageText);
        return editMessageText;
    }

    public List<SendMessage> getController(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message requestMessage = update.getMessage();

        long chatId = requestMessage != null ? requestMessage.getChatId() : callbackQuery.getFrom().getId();

        List<SendMessage> responseMessages;

        responseMessages = getTrainingsComponent.getTrainings(requestMessage, chatId);
        return responseMessages;
    }

    public SendMessage createController(Update update) {
        SendMessage responseMessage = new SendMessage();
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message requestMessage = update.getMessage();

        long chatId = requestMessage != null ? requestMessage.getChatId() : callbackQuery.getFrom().getId();

        responseMessage = registrationController(requestMessage, responseMessage);
        responseMessage = createUserController(callbackQuery, requestMessage, chatId, responseMessage);
        responseMessage = createTrainingController(callbackQuery, requestMessage, chatId, responseMessage);
        responseMessage = createExerciseController(callbackQuery, chatId, responseMessage);

        beforeMessages.put(chatId, responseMessage);
        return responseMessage;
    }

    private SendMessage registrationController(Message requestMessage, SendMessage responseMessage) {
        if (Objects.nonNull(requestMessage) && requestMessage.hasText() && "/start".equals(requestMessage.getText())) {
            return registrationComponent.registration(requestMessage);
        }
        return responseMessage;
    }

    private SendMessage createUserController(CallbackQuery callbackQuery, Message message, long chatId,
                                             SendMessage responseMessage) {
        SendMessage beforeMessage = beforeMessages.get(chatId);
        String data = (callbackQuery != null) ? callbackQuery.getData() : "";
        String textCommand = data.replaceAll("/.*", "");

        if ("createUser".equals(textCommand)) {
            return createUserComponent.createUser(callbackQuery, chatId);

        }

        if (beforeMessage == null) return responseMessage;
        if (!data.contains(CREATE_USER.getUrl()) && Objects.nonNull(callbackQuery)) {
            return responseMessage;
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

        return createUserComponent.createUser(callbackQuery, chatId);
    }

    private SendMessage createTrainingController(
            CallbackQuery callbackQuery,
            Message message, long chatId,
            SendMessage responseMessage
    ) {
        String data = (callbackQuery != null) ? callbackQuery.getData() : "";
        String textCommand = data.replaceAll("/.*", "");
        String textMessage = Objects.nonNull(message)
                ? message.getText()
                : "";

        if (textMessage.equals("/create_new_training")) {
            return createTrainingComponent.createOrGenerate(chatId);
        }

        if ("createNewTraining".equals(textCommand)) {
            return createTrainingComponent.createTraining(callbackQuery, chatId);
        } else if ("generateNewTraining".equals(textCommand)) {
            return createTrainingComponent.generateNewTraining(callbackQuery, chatId);
        }
        return responseMessage;
    }

    private SendMessage createExerciseController(CallbackQuery callbackQuery, long chatId, SendMessage responseMessage) {
        String data = (callbackQuery != null) ? callbackQuery.getData() : "";
        String textCommand = data.replaceAll("/.*", "");

        if (data.contains("done")) return responseMessage;
        if ("createNewExercise".equals(textCommand)) {
            return createExerciseComponent.createExercise(callbackQuery, chatId);
        }
        return responseMessage;
    }

    private CallbackQuery createNewCallbackQuery(Message message) {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        return callbackQuery;
    }

    private void setMessageCash(long chatId, EditMessageText editMessageText) {
        MessageCash messageCash = new MessageCash();
        messageCash.setChatId(chatId);
        messageCash.setEditMessageText(editMessageText);
        messageCash.setLocalDateTime(LocalDateTime.now());
        CashComponent.MESSAGE_CASH.add(messageCash);
    }

    private void setMessageCash(long chatId, SendMessage editMessageText) {
        MessageCash messageCash = new MessageCash();
        messageCash.setChatId(chatId);
        messageCash.setEditMessageText(editMessageText);
        messageCash.setLocalDateTime(LocalDateTime.now());
        CashComponent.MESSAGE_CASH.add(messageCash);
    }

    private void setMessageCash(long chatId, Message editMessageText) {
        MessageCash messageCash = new MessageCash();
        messageCash.setChatId(chatId);
        messageCash.setEditMessageText(editMessageText);
        messageCash.setLocalDateTime(LocalDateTime.now());
        CashComponent.MESSAGE_CASH.add(messageCash);
    }

    private EditMessageText getLastMessageByChatId(long chatId, Message actualMessage) {
        MessageCash messageCash = CashComponent.MESSAGE_CASH.stream()
                .filter(message -> message.getChatId() == chatId)
                .filter(message -> !message.getEditMessageText().getReplyMarkup().equals(actualMessage.getReplyMarkup()))
                .max(Comparator.comparing(MessageCash::getLocalDateTime))
                .orElseThrow(() -> new NotFoundException("Не найдено сообщение с chatId = %s"
                        .formatted(chatId)));
        CashComponent.MESSAGE_CASH.remove(messageCash);
        return messageCash.getEditMessageText();
    }

}