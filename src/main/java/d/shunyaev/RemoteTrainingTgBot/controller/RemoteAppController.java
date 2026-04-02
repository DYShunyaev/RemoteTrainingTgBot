package d.shunyaev.RemoteTrainingTgBot.controller;

import d.shunyaev.RemoteTrainingTgBot.config.RemoteAppApiClient;
import d.shunyaev.api.ExerciseControllerApi;
import d.shunyaev.api.TrainingControllerApi;
import d.shunyaev.api.UserControllerApi;
import d.shunyaev.api.UserInfoControllerApi;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoteAppController {

    @Getter
    private static final RemoteAppApiClient CLIENT = new RemoteAppApiClient();
    private static UserControllerApi userControllerApi;
    private static UserInfoControllerApi userInfoControllerApi;
    private static TrainingControllerApi trainingControllerApi;
    private static ExerciseControllerApi exerciseControllerApi;

    public static UserControllerApi getUserControllerApi() {
        if (userControllerApi == null) {
            userControllerApi = CLIENT.buildClient(UserControllerApi.class);
        }
        return userControllerApi;
    }

    public static UserInfoControllerApi getUserInfoControllerApi() {
        if (userInfoControllerApi == null) {
            userInfoControllerApi = CLIENT.buildClient(UserInfoControllerApi.class);
        }
        return userInfoControllerApi;
    }

    public static TrainingControllerApi getTrainingControllerApi() {
        if (trainingControllerApi == null) {
            trainingControllerApi = CLIENT.buildClient(TrainingControllerApi.class);
        }
        return trainingControllerApi;
    }

    public static ExerciseControllerApi getExerciseControllerApi() {
        if (exerciseControllerApi == null) {
            exerciseControllerApi = CLIENT.buildClient(ExerciseControllerApi.class);
        }
        return exerciseControllerApi;
    }
}
