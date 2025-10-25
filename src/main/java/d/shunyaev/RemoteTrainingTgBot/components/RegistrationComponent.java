package d.shunyaev.RemoteTrainingTgBot.components;

import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.model.RequestContainerCreateUserRequest;
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
    boolean isRegistration(Message message) {
        if (validateComponent.isExistUser(message.getChatId())) {
            User user = message.getFrom();

            UsersBot usersBot = new UsersBot()
                    .setChatId(message.getChatId())
                    .setUserName(user.getUserName())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName() != null
                            ? user.getLastName()
                            : user.getFirstName());

            usersBotRepository.setNewUser(usersBot);

            CashComponent.CREATE_USER_REQUESTS.put(message.getChatId(), new RequestContainerCreateUserRequest());
            return true;
        } else {
            return false;
        }
    }

    public SendMessage registration(Message message) {
        SendMessage responseMessage = new SendMessage();
        if (isRegistration(message)) {
            String helloText = "helloText";
            responseMessage.setChatId(message.getChatId());
            responseMessage.setText(helloText);

            InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("REGISTRATION");
            button.setCallbackData(CREATE_USER.getUrl());

            List<InlineKeyboardButton> buttonList = new ArrayList<>();
            buttonList.add(button);

            keyboard.add(buttonList);

            markupInLine.setKeyboard(keyboard);
            responseMessage.setReplyMarkup(markupInLine);

        } else {
            responseMessage.setChatId(message.getChatId());
            responseMessage.setText("Пользователь уже зарегестрирован.");
        }
        return responseMessage;
    }

}
