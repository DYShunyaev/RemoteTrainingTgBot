package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetUserInfoComponent;
import d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

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


    public SendMessage setTrainer(CallbackQuery callbackQuery, long chatId, EditMessageText editMessageText) {
        String data = "";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String trainerUserName = data.replaceAll(ServicesUrl.SET_MY_TRAINER.getUrl(), "");

        UsersBot trainer = usersBotRepository.getUserBotByUserName(trainerUserName);
        if (Objects.isNull(trainer)) {
            sendMessage.setText("Пользователь %s не зарегестрирован."
                    .formatted(trainerUserName));
            return sendMessage;
        }

        return sendMessage;
    }
}
