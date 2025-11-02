package d.shunyaev.RemoteTrainingTgBot.components.getters_components;

import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.model.RequestContainerGetTrainingsRequest;
import d.shunyaev.model.ResponseContainerGetTrainingsResponse;
import org.springframework.stereotype.Component;

@Component
public class GetTrainingsComponent {

    private final UsersBotRepository usersBotRepository;
    private final RequestContainerGetTrainingsRequest request = new RequestContainerGetTrainingsRequest();

    public GetTrainingsComponent(UsersBotRepository usersBotRepository) {
        this.usersBotRepository = usersBotRepository;
    }

    public ResponseContainerGetTrainingsResponse getTrainingsByChatId(long chatId) {
        UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);
        request
                .userId(usersBot.getUserId())
                .dateOfTraining(null)
                .dayOfWeek(null);
        return RemoteAppController.getTrainingControllerApi().getTrainings(request);
    }
}
