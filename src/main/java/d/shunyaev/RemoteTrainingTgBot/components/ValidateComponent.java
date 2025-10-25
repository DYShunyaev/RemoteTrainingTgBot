package d.shunyaev.RemoteTrainingTgBot.components;

import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ValidateComponent {

    private final UsersBotRepository usersBotRepository;

    public ValidateComponent(UsersBotRepository usersBotRepository) {
        this.usersBotRepository = usersBotRepository;
    }

    public boolean isExistUser(long chatId) {
        return Objects.isNull(usersBotRepository.getUserBotByChatId(chatId));
    }
}
