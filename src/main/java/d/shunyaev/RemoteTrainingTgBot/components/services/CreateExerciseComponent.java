package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.ValidateComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetTrainingsComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetUserInfoComponent;
import d.shunyaev.model.RequestContainerCreateExerciseRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Optional;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.CREATE_NEW_EXERCISE;

@Component
public class CreateExerciseComponent {

    private final ValidateComponent validateComponent;
    private final GetUserInfoComponent getUserInfoComponent;
    private final GetTrainingsComponent getTrainingsComponent;

    public CreateExerciseComponent(
            ValidateComponent validateComponent,
            GetUserInfoComponent getUserInfoComponent,
            GetTrainingsComponent getTrainingsComponent
    ) {
        this.validateComponent = validateComponent;
        this.getUserInfoComponent = getUserInfoComponent;
        this.getTrainingsComponent = getTrainingsComponent;
    }

    public SendMessage createExercise(CallbackQuery callbackQuery, long chatId) {
        RequestContainerCreateExerciseRequest req = getExerciseRequest(chatId);
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);

        String data = Optional.ofNullable(callbackQuery)
                .map(CallbackQuery::getData)
                .map(d -> d.replaceAll(CREATE_NEW_EXERCISE.getUrl(), ""))
                .orElse("");


        return responseMessage;
    }

    private RequestContainerCreateExerciseRequest getExerciseRequest(long chatId) {
        return CashComponent.CREATE_EXERCISE_REQUEST.computeIfAbsent(
                chatId, k -> new RequestContainerCreateExerciseRequest());
    }
}
