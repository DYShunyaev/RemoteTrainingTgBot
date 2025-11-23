package d.shunyaev.RemoteTrainingTgBot.utils;

import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.BadRequestException;
import d.shunyaev.model.ResponseContainerResult;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class CallServerHelper {

    public ResponseContainerResult callRemoteTrainingApp(Supplier<ResponseContainerResult> action) {
        try {
            return action.get();
        } catch (BadRequestException e) {
            return e.getResponseBody();
        }
    }
}

