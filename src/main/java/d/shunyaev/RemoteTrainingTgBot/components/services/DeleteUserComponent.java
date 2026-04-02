package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetUserInfoComponent;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.RemoteTrainingTgBot.utils.CallServerHelper;
import d.shunyaev.RemoteTrainingTgBot.utils.CreateButtonHelper;
import d.shunyaev.model.RequestContainerDeleteUserRequest;
import d.shunyaev.model.ResponseContainerResult;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.DELETE_USER;

@Component
public class DeleteUserComponent {

    private final UsersBotRepository usersBotRepository;
    private final GetUserInfoComponent getUserInfoComponent;

    public DeleteUserComponent(
            UsersBotRepository usersBotRepository,
            GetUserInfoComponent getUserInfoComponent
    ) {
        this.usersBotRepository = usersBotRepository;
        this.getUserInfoComponent = getUserInfoComponent;
    }

    public EditMessageText deleteUser(EditMessageText editMessageText, long chatId, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (!data.contains(DELETE_USER.getUrl())) {
            return editMessageText;
        }

        data = data.replaceAll(DELETE_USER.getUrl(), "");

        switch (data) {
            case "YES" -> deleteUser(editMessageText, chatId);
            case "NOT" -> returnMyData(editMessageText, chatId);
            default -> chooseRequest(editMessageText);
        }

        return editMessageText;
    }

    private void deleteUser(EditMessageText editMessageText, long chatId) {
        UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);
        ResponseContainerResult result = CallServerHelper.callRemoteTrainingApp(
                () -> RemoteAppController.getUserControllerApi().deleteUser(
                        new RequestContainerDeleteUserRequest()
                                .userId(usersBot.getUserId())
                )
        );
        if (result.getCode() != 200) {
            editMessageText.setText("Ошибка удаления, повторите попытку.");
        }
        usersBotRepository.deleteUserBot(chatId);
        editMessageText.setText("Все Ваши данные были удалены! \uD83E\uDD7A \n" +
                "Чтобы заново начать пользоваться ботом используйте команду \"/start\".\n" +
                "До новых встреч! ☺️");
        editMessageText.setReplyMarkup(
                CreateButtonHelper.addMarkupButton(
                        "Начать пользоваться ботом заново!",
                        "/start"
                )
        );
    }

    private void returnMyData(EditMessageText editMessageText, long chatId) {
        getUserInfoComponent.getMyData(editMessageText, chatId);
    }

    private void chooseRequest(EditMessageText editMessageText) {
        editMessageText.setText("Вы уверены, что хотите удалить все данные?\n" +
                "Будут удалены все записи о тренировках + вся личная информация");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(
                List.of(
                        CreateButtonHelper.createButton(
                                DELETE_USER.getUrl() + "YES",
                                "Да ✅"
                        ),
                        CreateButtonHelper.createButton(
                                DELETE_USER.getUrl() + "NOT",
                                "Нет ❌"
                        )
                )
        );
        markup.setKeyboard(keyboard);
        editMessageText.setReplyMarkup(markup);
    }
}
