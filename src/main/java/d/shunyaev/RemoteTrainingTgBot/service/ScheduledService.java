package d.shunyaev.RemoteTrainingTgBot.service;

import d.shunyaev.RemoteTrainingTgBot.components.getters_components.TrainingsSteps;
import d.shunyaev.RemoteTrainingTgBot.components.services.GetTrainingsComponent;
import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.model.Trainings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class ScheduledService {

    private final UsersBotRepository usersBotRepository;
    private final TrainingsSteps trainingsSteps;
    private final GetTrainingsComponent getTrainingsComponent;
    private final TelegramBot telegramBot;

    public ScheduledService(
            UsersBotRepository usersBotRepository,
            TrainingsSteps trainingsSteps,
            GetTrainingsComponent getTrainingsComponent,
            TelegramBot telegramBot) {
        this.usersBotRepository = usersBotRepository;
        this.trainingsSteps = trainingsSteps;
        this.getTrainingsComponent = getTrainingsComponent;
        this.telegramBot = telegramBot;
    }

    private List<SendMessage> pushTrainings() {
        List<UsersBot> usersBots = usersBotRepository.getAllUsers();
        List<SendMessage> sendingMessages = new ArrayList<>();

        for (UsersBot usersBot : usersBots) {
            List<Trainings> trainings = trainingsSteps.getTrainingsByChatId(usersBot.getChatId())
                    .getTrainings();
            LocalDate localDate = LocalDate.now();
            for (Trainings training : Objects.requireNonNull(trainings)) {
                LocalDate trainingDate = LocalDate.parse(
                        Objects.requireNonNull(training.getDate()),
                        DateTimeFormatter.ISO_DATE
                );
                if (trainingDate.equals(localDate)) {
                    sendingMessages.add(getTrainingsComponent.createTrainingMessage(training, usersBot.getChatId()));
                }
            }
        }
        return sendingMessages;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    private void initTask() {
        log.info("ScheduledService начал работу. {}", LocalDateTime.now());
        List<SendMessage> sendMessages = pushTrainings();

        for (SendMessage sendMessage : sendMessages) {
            SendMessage firstMessage = new SendMessage();
            firstMessage.setChatId(sendMessage.getChatId());
            firstMessage.setText("Привет! \uD83D\uDC4B"
                    + "\nНе забудь про сегодняшнюю тренрировку! \uD83C\uDFCB️");

            telegramBot.sendMessages(List.of(
                    firstMessage,
                    sendMessage
            ));
        }
    }

}
