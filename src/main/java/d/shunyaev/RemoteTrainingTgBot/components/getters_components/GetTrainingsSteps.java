package d.shunyaev.RemoteTrainingTgBot.components.getters_components;

import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.model.RequestContainerGetTrainingsRequest;
import d.shunyaev.model.RequestContainerTrainingIsDoneRequest;
import d.shunyaev.model.ResponseContainerGetTrainingsResponse;
import d.shunyaev.model.ResponseContainerResult;
import org.springframework.stereotype.Component;

@Component
public class GetTrainingsSteps {

    private final UsersBotRepository usersBotRepository;
    private final RequestContainerGetTrainingsRequest request = new RequestContainerGetTrainingsRequest();

    public GetTrainingsSteps(UsersBotRepository usersBotRepository) {
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

    public ResponseContainerResult setTrainingIsDone(long chatId, long trainingId) {
        UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);
        var request = new RequestContainerTrainingIsDoneRequest()
                .userId(usersBot.getUserId())
                .trainingId(trainingId);
        return RemoteAppController.getTrainingControllerApi().trainingIsDone(request);
    }
}
