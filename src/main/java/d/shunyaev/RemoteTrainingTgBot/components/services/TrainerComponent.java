package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetUserInfoComponent;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.RemoteTrainingTgBot.utils.CallServerHelper;
import d.shunyaev.model.RequestContainerSetTrainerRequest;
import d.shunyaev.model.ResponseContainerResult;
import d.shunyaev.model.UserData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Objects;

@Component
public class TrainerComponent {

    private final UsersBotRepository usersBotRepository;
    private final GetUserInfoComponent getUserInfoComponent;

    public TrainerComponent(
            UsersBotRepository usersBotRepository,
            GetUserInfoComponent getUserInfoComponent
    ) {
        this.usersBotRepository = usersBotRepository;
        this.getUserInfoComponent = getUserInfoComponent;
    }

    public SendMessage setTrainer(SendMessage responseMessage, long chatId) {
        String text = "Введите user_name (имя пользователя Telegram) своего тренера:";
        responseMessage.setChatId(chatId);
        if (Objects.nonNull(responseMessage.getText()) && !responseMessage.getText().isEmpty()) {
            responseMessage.setText(responseMessage.getText() + "\n" + text);
        } else {
            responseMessage.setText(text);
        }
        return responseMessage;
    }


    public SendMessage setTrainer(SendMessage responseMessage, Message message, long chatId) {
        responseMessage.setChatId(chatId);
        String trainerUserName = message.getText().replaceAll("@","");

        UsersBot trainerBot = usersBotRepository.getUserBotByUserName(trainerUserName);
        if (Objects.isNull(trainerBot)) {
            responseMessage.setText("Пользователь %s не зарегестрирован."
                    .formatted(trainerUserName));
            return responseMessage;
        }

        UserData user = getUserInfoComponent.getUserInfo(message.getFrom().getUserName());
        UserData trainer = getUserInfoComponent.getUserInfo(trainerUserName);

        ResponseContainerResult result = CallServerHelper.callRemoteTrainingApp(
                () -> RemoteAppController.getUserControllerApi().setTrainer(
                        new RequestContainerSetTrainerRequest()
                                .trainerId(trainer.getUserId())
                                .userId(user.getUserId())
                )
        );

        responseMessage.setText("Тренер успешно добавлен!");

        if (Objects.equals(result.getCode(), 200)) {
            return responseMessage;
        } else if (Objects.equals(result.getCode(), -14)) {
            responseMessage.setText(Objects.requireNonNull(result.getMessage())
                    .replaceAll("пользователя", "вас"));
            return responseMessage;
        } else {
            responseMessage.setText(Objects.requireNonNull(result.getMessage()));
            return setTrainer(responseMessage, chatId);
        }
    }
}
