package d.shunyaev.RemoteTrainingTgBot.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class RandomUtils {

    public Long generateRandomChatId() {
        Random random = new Random();
        return 1000000000L + (long)(random.nextDouble() * 9000000000L);
    }
}
