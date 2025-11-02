package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.ValidateComponent;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.model.RequestContainerCreateUserRequest;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.CREATE_USER;

@Component
public class RegistrationComponent {

    private final UsersBotRepository usersBotRepository;
    private final ValidateComponent validateComponent;

    public RegistrationComponent(UsersBotRepository usersBotRepository, ValidateComponent validateComponent) {
        this.usersBotRepository = usersBotRepository;
        this.validateComponent = validateComponent;
    }

    @Transactional
    boolean isRegistration(Message message, long chatId) {
        if (validateComponent.isExistUser(chatId)) {
            User user = message.getFrom();

            UsersBot usersBot = new UsersBot()
                    .setChatId(chatId)
                    .setUserName(user.getUserName())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName() != null
                            ? user.getLastName()
                            : user.getFirstName());

            usersBotRepository.setNewUser(usersBot);

            return true;
        } else {
            return false;
        }
    }

    public SendMessage registration(@NonNull Message message) {
        return registration(message, message.getChatId());
    }

    public SendMessage registration(@NonNull Message message, long chatId) {
        SendMessage responseMessage = new SendMessage();
        if (isRegistration(message, chatId)) {
            responseMessage = addRegistrationButton(responseMessage, chatId);
        } else {
            responseMessage.setChatId(chatId);
            responseMessage.setText("Пользователь уже зарегестрирован.");
        }
        return responseMessage;
    }

    public SendMessage addRegistrationButton(@NonNull SendMessage responseMessage, long chatId) {
        String helloText = "helloText";
        responseMessage.setChatId(chatId);
        responseMessage.setText(helloText);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("РЕГИСТРАЦИЯ");
        button.setCallbackData(CREATE_USER.getUrl());

        keyboard.add(List.of(button));

        markupInLine.setKeyboard(keyboard);
        responseMessage.setReplyMarkup(markupInLine);
        CashComponent.CREATE_USER_REQUESTS.put(chatId, new RequestContainerCreateUserRequest());
        return responseMessage;
    }

}
