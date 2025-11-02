package d.shunyaev.RemoteTrainingTgBot.components.getters_components;

import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.model.RequestContainerGetUserByUserNameRequest;
import d.shunyaev.model.ResponseContainerGetUsersResponse;
import d.shunyaev.model.UserData;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class GetUserInfoComponent {

    private final RequestContainerGetUserByUserNameRequest request = new RequestContainerGetUserByUserNameRequest();

    public UserData getUserInfo(@NonNull String userName) {
        request.setUserName(userName);
        ResponseContainerGetUsersResponse response = RemoteAppController
                .getUserInfoControllerApi().getUsersByUserName(request);
        return response.getUsers()
                .stream()
                .findFirst()
                .get();
    }

    public Long getUserId(@NonNull String userName) {
        request.setUserName(userName);
        ResponseContainerGetUsersResponse response = RemoteAppController
                .getUserInfoControllerApi().getUsersByUserName(request);
        return response.getUsers()
                .stream()
                .findFirst()
                .get()
                .getUserId();
    }
}
