package d.shunyaev.RemoteTrainingTgBot.components;

import d.shunyaev.model.*;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class CashComponent {

    public static final Map<Long, RequestContainerCreateUserRequest> CREATE_USER_REQUESTS = new HashMap<>();
    public static final Map<Long, RequestContainerCreateTrainingRequest> CREATE_TRAINING_REQUESTS = new HashMap<>();
    public static final Map<Long, RequestContainerGenerateTrainingRequest> GENERATE_TRAINING_REQUESTS = new HashMap<>();
    public static final Map<Long, RequestContainerCreateExerciseRequest> CREATE_EXERCISE_REQUEST = new HashMap<>();
    public static final Map<Long, RequestContainerUpdateExerciseRequest> UPDATE_EXERCISE_REQUEST = new HashMap<>();
}
