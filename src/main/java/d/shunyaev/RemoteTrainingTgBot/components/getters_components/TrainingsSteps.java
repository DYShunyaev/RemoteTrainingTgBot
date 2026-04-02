package d.shunyaev.RemoteTrainingTgBot.components.getters_components;

import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.model.*;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class TrainingsSteps {

    private final UsersBotRepository usersBotRepository;
    private final RequestContainerGetTrainingsRequest request = new RequestContainerGetTrainingsRequest();

    public TrainingsSteps(UsersBotRepository usersBotRepository) {
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

    public Trainings getTrainingByTrainingId(long chatId, long trainingId) {
        UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);
        request
                .userId(usersBot.getUserId())
                .dateOfTraining(null)
                .dayOfWeek(null);
        return RemoteAppController.getTrainingControllerApi().getTrainings(request)
                .getTrainings()
                .stream()
                .filter(training -> training.getTrainingId().equals(trainingId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Не найдено тренировки"));
    }

    public ResponseContainerResult setTrainingIsDone(long chatId, long trainingId) {
        UsersBot usersBot = usersBotRepository.getUserBotByChatId(chatId);
        var request = new RequestContainerTrainingIsDoneRequest()
                .userId(usersBot.getUserId())
                .trainingId(trainingId);
        return RemoteAppController.getTrainingControllerApi().trainingIsDone(request);
    }
}
