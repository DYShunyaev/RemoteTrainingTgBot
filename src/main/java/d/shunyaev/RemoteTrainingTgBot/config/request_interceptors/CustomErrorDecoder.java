package d.shunyaev.RemoteTrainingTgBot.config.request_interceptors;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.SneakyThrows;

import static feign.FeignException.errorStatus;

public class CustomErrorDecoder implements ErrorDecoder {
    @SneakyThrows
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 400 && response.body() != null) {
            return new BadRequestException(Util.toByteArray(response.body().asInputStream()));
        }
        return errorStatus(methodKey, response);
    }
}
