package d.shunyaev.RemoteTrainingTgBot.components.getters_components;

import d.shunyaev.RemoteTrainingTgBot.components.services.RegistrationComponent;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.BadRequestException;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.RemoteTrainingTgBot.utils.CreateButtonHelper;
import d.shunyaev.model.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GetUserInfoComponent {

    private final UsersBotRepository usersBotRepository;
    private final RegistrationComponent registrationComponent;
    private final RemoteAppController remoteAppController;

    private final RequestContainerGetUserByUserNameRequest request = new RequestContainerGetUserByUserNameRequest();


    public UserData getUserInfo(@NonNull String userName) {
        request.setUserName(userName);
        ResponseContainerGetUsersResponse response = remoteAppController
                .getUserInfoControllerApi().getUsersByUserName(request);
        return response.getUsers()
                .stream()
                .findFirst()
                .get();
    }

    public Long getUserId(@NonNull String userName) {
        request.setUserName(userName);
        ResponseContainerGetUsersResponse response = remoteAppController
                .getUserInfoControllerApi().getUsersByUserName(request);
        return response.getUsers()
                .stream()
                .findFirst()
                .get()
                .getUserId();
    }

    public List<SendMessage> getUserTrainer(List<SendMessage> responseList, Message requestMessage, long chatId) {
        if (!requestMessage.getText().contains("/get_my_trainer")) {
            return responseList;
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);
        UserData userData = null;
        ResponseContainerResult errorResult = null;
        try {
            userData = remoteAppController.getUserInfoControllerApi().getUserTrainer(
                            new RequestContainerGetUserTrainerRequest()
                                    .userId(usersBot.getUserId())
                    )
                    .getUsers()
                    .stream()
                    .findFirst()
                    .orElse(null);
        } catch (BadRequestException e) {
            errorResult = e.getResponseBody();
        }

        if (Objects.isNull(userData) || Objects.nonNull(errorResult)) {
            sendMessage.setText("У вас нет тренера.");
            sendMessage.setReplyMarkup(
                    CreateButtonHelper.addMarkupButton(
                            "Добавить тренера",
                            "/set_my_trainer"
                    )
            );
            return List.of(sendMessage);
        }

        String text = " Ваш тренер:\n" + getUserText(userData);
        sendMessage.setText(text);
        return List.of(sendMessage);
    }

    public List<SendMessage> getMyData(List<SendMessage> responseList, Message requestMessage, long chatId) {
        if (!requestMessage.getText().contains("/get_my_data")) {
            return responseList;
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);
        if (Objects.isNull(usersBot)) {
            sendMessage.setText("Пользователь не зарегистрирован");
            return List.of(registrationComponent.addRegistrationButton(sendMessage, chatId));
        }
        UserData userData = getUserInfo(usersBot.getUserName());

        String text = " Ваши данные:\n" + getUserText(userData) + "\n" +
                "Ваши цели: %s\n".formatted(userData.getGoals()) +
                "Ваш рост: %s\n".formatted(userData.getHeight()) +
                "Ваш вес: %s".formatted(userData.getWeight());

        sendMessage.setText(text);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(
                CreateButtonHelper.createButtonList(
                        "Изменить данные",
                        ServicesUrl.UPDATE_USER.getUrl()
                )
        );
        keyboard.add(
                CreateButtonHelper.createButtonList(
                        "Удалить данные",
                        ServicesUrl.DELETE_USER.getUrl()
                )
        );
        markup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(markup);
        return List.of(sendMessage);
    }

    public void getMyData(EditMessageText editMessageText, long chatId) {
        UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);

        UserData userData = getUserInfo(usersBot.getUserName());

        String text = "Ваши данные:\n" + getUserText(userData) + "\n" +
                "Ваши цели: %s\n".formatted(userData.getGoals()) +
                "Ваш рост: %s\n".formatted(userData.getHeight()) +
                "Ваш вес: %s".formatted(userData.getWeight());

        editMessageText.setText(text);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(
                CreateButtonHelper.createButtonList(
                        "Изменить данные",
                        ServicesUrl.UPDATE_USER.getUrl()
                )
        );
        keyboard.add(
                CreateButtonHelper.createButtonList(
                        "Удалить данные",
                        ServicesUrl.DELETE_USER.getUrl()
                )
        );
        markup.setKeyboard(keyboard);
        editMessageText.setReplyMarkup(markup);
    }

    private String getUserText(UserData userData) {
        return "Имя пользователя: @%s\n".formatted(userData.getUserName()) +
                "Имя: %s\n".formatted(userData.getFirstName()) +
                "Фамилия: %s\n".formatted(userData.getLastName()) +
                "Email: %s\n".formatted(userData.getEmail()) +
                "Дата рождения: %s\n".formatted(userData.getDateOfBirth()) +
                "Возраст: %s лет\n".formatted(userData.getAge()) +
                "Уровень подготовки: %s".formatted(userData.getTrainingLevel());
    }
}
