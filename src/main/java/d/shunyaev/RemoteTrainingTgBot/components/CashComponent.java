package d.shunyaev.RemoteTrainingTgBot.components;

import d.shunyaev.model.RequestContainerCreateUserRequest;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class CashComponent {

    public static final Map<Long, RequestContainerCreateUserRequest> CREATE_USER_REQUESTS = new HashMap<>();
}
