package d.shunyaev.RemoteTrainingTgBot.controller;

import d.shunyaev.RemoteTrainingTgBot.config.RemoteAppApiClient;
import d.shunyaev.api.ExerciseControllerApi;
import d.shunyaev.api.TrainingControllerApi;
import d.shunyaev.api.UserControllerApi;
import d.shunyaev.api.UserInfoControllerApi;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoteAppController {

    private final RemoteAppApiClient client;
    private UserControllerApi userControllerApi;
    private UserInfoControllerApi userInfoControllerApi;
    private TrainingControllerApi trainingControllerApi;
    private ExerciseControllerApi exerciseControllerApi;

    @PostConstruct
    public void init() {
        this.userControllerApi = client.buildClient(UserControllerApi.class);
        this.userInfoControllerApi = client.buildClient(UserInfoControllerApi.class);
        this.trainingControllerApi = client.buildClient(TrainingControllerApi.class);
        this.exerciseControllerApi = client.buildClient(ExerciseControllerApi.class);
    }

    public UserControllerApi getUserControllerApi() {
        return userControllerApi;
    }

    public UserInfoControllerApi getUserInfoControllerApi() {
        return userInfoControllerApi;
    }

    public TrainingControllerApi getTrainingControllerApi() {
        return trainingControllerApi;
    }

    public ExerciseControllerApi getExerciseControllerApi() {
        return exerciseControllerApi;
    }

}
