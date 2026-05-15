package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.BuildGridComponent;
import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetUserInfoComponent;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.RemoteTrainingTgBot.utils.CallServerHelper;
import d.shunyaev.RemoteTrainingTgBot.utils.CreateButtonHelper;
import d.shunyaev.model.RequestContainerUpdateUserRequest;
import d.shunyaev.model.UserData;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.DELETE_USER;
import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.UPDATE_USER;

@Component
@RequiredArgsConstructor
public class UpdateUserComponent {

    private final GetUserInfoComponent getUserInfoComponent;
    private final BuildGridComponent buildGridComponent;
    private final UsersBotRepository usersBotRepository;
    private final RemoteAppController remoteAppController;
    private UserData userData;

    public EditMessageText updateUser(CallbackQuery callbackQuery, long chatId, EditMessageText editMessageText) {
        String data = callbackQuery.getData();
        if (!data.contains(UPDATE_USER.getUrl()) || data.contains(DELETE_USER.getUrl())) {
            return editMessageText;
        }

        userData = getUserInfoComponent.getUserInfo(callbackQuery.getFrom().getUserName());
        data = data.replaceAll(UPDATE_USER.getUrl(), "");

        VariablesUpdateUser variablesUpdateUser = VariablesUpdateUser.getByUrl(data
                .replaceAll("/.*", "/"));
        switch (variablesUpdateUser) {
            case GOALS -> chooseNewGoals(editMessageText, chatId, data);
            case TRAINING_LEVEL -> chooseNewTrainingLevel(editMessageText, chatId, data);
            case HEIGHT -> chooseHeight(editMessageText, chatId, data);
            case WEIGHT -> chooseWeight(editMessageText, chatId, data);
            case DONE -> done(editMessageText, chatId);
            default -> chooseVariables(editMessageText, chatId);
        }

        return editMessageText;
    }

    private void done(EditMessageText editMessageText, long chatId) {
        RequestContainerUpdateUserRequest req = createOrGetUpUserReq(chatId);
        if (Objects.nonNull(req.getGoals()) ||
                Objects.nonNull(req.getTrainingLevel()) ||
                Objects.nonNull(req.getHeight()) ||
                Objects.nonNull(req.getWeight())
        ) {
            UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);
            req.setUserId(usersBot.getUserId());
            CallServerHelper.callRemoteTrainingApp(
                    () -> remoteAppController.getUserControllerApi().updateUser(req)
            );
        }

        getUserInfoComponent.getMyData(editMessageText, chatId);
    }

    private void chooseHeight(EditMessageText editMessageText, long chatId, String data) {
        String callback = "height/";
        data = data.replaceAll(VariablesUpdateUser.HEIGHT.url.formatted(""), "");
        RequestContainerUpdateUserRequest req = createOrGetUpUserReq(chatId);

        if (data.replaceAll(callback, "").matches("\\d*") && !data.isEmpty()) {
            long height = Long.parseLong(data.replaceAll(callback, ""));
            req.setHeight(height);
            CashComponent.UPDATE_USER_REQUEST.put(chatId, req);

            chooseVariables(editMessageText, chatId);
            return;
        }

        editMessageText.setText("Выберете подходящее, текущий рост: %s"
                .formatted(userData.getHeight()));
        editMessageText.setReplyMarkup(buildGridComponent.buildGrid(data, callback,
                List.of("150-200", "201-250"), VariablesUpdateUser.HEIGHT.getUrl().formatted("")));
    }

    private void chooseWeight(EditMessageText editMessageText, long chatId, String data) {
        String callback = "weight/";
        data = data.replaceAll(VariablesUpdateUser.WEIGHT.url.formatted(""), "");
        RequestContainerUpdateUserRequest req = createOrGetUpUserReq(chatId);

        if (data.replaceAll(callback, "").matches("\\d*") && !data.isEmpty()) {
            long weight = Long.parseLong(data.replaceAll(callback, ""));
            req.setWeight(weight);
            CashComponent.UPDATE_USER_REQUEST.put(chatId, req);

            chooseVariables(editMessageText, chatId);
            return;
        }

        editMessageText.setText("Выберете подходящее, текущий вес: %s"
                .formatted(userData.getWeight()));
        editMessageText.setReplyMarkup(buildGridComponent.buildGrid(data, callback,
                List.of("41-80", "81-120", "121-150"), VariablesUpdateUser.WEIGHT.getUrl().formatted("")));
    }

    private void chooseNewTrainingLevel(EditMessageText editMessageText, long chatId, String data) {
        data = data.replaceAll(VariablesUpdateUser.TRAINING_LEVEL.url.formatted(""), "");
        RequestContainerUpdateUserRequest req = createOrGetUpUserReq(chatId);

        if (!data.isEmpty()) {
            req.setTrainingLevel(RequestContainerUpdateUserRequest.TrainingLevelEnum.fromValue(data));
            CashComponent.UPDATE_USER_REQUEST.put(chatId, req);

            chooseVariables(editMessageText, chatId);
            return;
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (RequestContainerUpdateUserRequest.TrainingLevelEnum trainingLevel :
                RequestContainerUpdateUserRequest.TrainingLevelEnum.values()) {
            if (trainingLevel.getValue().equals(userData.getTrainingLevel())) {
                continue;
            }
            keyboard.add(
                    CreateButtonHelper.createButtonList(
                            trainingLevel.getValue(),
                            VariablesUpdateUser.TRAINING_LEVEL.getUrl()
                                    .formatted(trainingLevel.getValue())
                    )
            );
        }
        markup.setKeyboard(keyboard);
        editMessageText.setReplyMarkup(markup);
        editMessageText.setText("Выберете подходящее, текущий уровень подготовки: %s"
                .formatted(userData.getTrainingLevel()));
    }

    private void chooseNewGoals(EditMessageText editMessageText, long chatId, String data) {
        data = data.replaceAll(VariablesUpdateUser.GOALS.url.formatted(""), "");
        RequestContainerUpdateUserRequest req = createOrGetUpUserReq(chatId);

        if (!data.isEmpty()) {
            req.setGoals(RequestContainerUpdateUserRequest.GoalsEnum.fromValue(data));
            CashComponent.UPDATE_USER_REQUEST.put(chatId, req);

            chooseVariables(editMessageText, chatId);
            return;
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (RequestContainerUpdateUserRequest.GoalsEnum goals : RequestContainerUpdateUserRequest.GoalsEnum.values()) {
            if (goals.getValue().equals(userData.getGoals())) {
                continue;
            }
            keyboard.add(
                    CreateButtonHelper.createButtonList(
                            goals.getValue(),
                            VariablesUpdateUser.GOALS.getUrl()
                                    .formatted(goals.getValue())
                    )
            );
        }
        markup.setKeyboard(keyboard);
        editMessageText.setReplyMarkup(markup);
        editMessageText.setText("Выберете подходящее, текущие цели: %s"
                .formatted(userData.getGoals()));
    }

    private void chooseVariables(EditMessageText editMessageText, long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        RequestContainerUpdateUserRequest req = createOrGetUpUserReq(chatId);

        for (VariablesUpdateUser var : VariablesUpdateUser.values()) {
            if (var.equals(VariablesUpdateUser.NON) ||
                    var.equals(VariablesUpdateUser.GOALS) && Objects.nonNull(req.getGoals()) ||
                    var.equals(VariablesUpdateUser.TRAINING_LEVEL) && Objects.nonNull(req.getTrainingLevel()) ||
                    var.equals(VariablesUpdateUser.HEIGHT) && Objects.nonNull(req.getHeight()) ||
                    var.equals(VariablesUpdateUser.WEIGHT) && Objects.nonNull(req.getWeight())
            ) {
                continue;
            }
            keyboard.add(
                    CreateButtonHelper.createButtonList(
                            var.getDescription(),
                            var.getUrl().formatted("")
                    )
            );
        }
        markup.setKeyboard(keyboard);
        editMessageText.setReplyMarkup(markup);
        editMessageText.setText("Выберете подходящее:");
    }

    private RequestContainerUpdateUserRequest createOrGetUpUserReq(long chatId) {
        return CashComponent.UPDATE_USER_REQUEST.computeIfAbsent(
                chatId, k -> new RequestContainerUpdateUserRequest());
    }

    private enum VariablesUpdateUser {
        GOALS("changeGoals/%s", "Изменить цели"),
        TRAINING_LEVEL("changeTrainingLevel/%s", "Изменить уровень подготовки"),
        WEIGHT("changeWeight/%s", "Изменить вес"),
        HEIGHT("changeHeight/%s", "Изменить рост"),
        DONE("done", "Завершить ✅"),
        NON("", "");
        private final String url;

        private String getDescription() {
            return description;
        }

        private final String description;

        private String getUrl() {
            return UPDATE_USER.getUrl() + this.url;
        }

        VariablesUpdateUser(String url, String description) {
            this.url = url;
            this.description = description;
        }

        private static VariablesUpdateUser getByUrl(@NotNull String url) {
            if (url.isEmpty()) return NON;
            return Arrays.stream(VariablesUpdateUser.values())
                    .filter(var -> var.url.contains(url))
                    .findFirst()
                    .orElse(NON);
        }
    }
}
