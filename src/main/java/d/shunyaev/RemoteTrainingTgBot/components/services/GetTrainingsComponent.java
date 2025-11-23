package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.getters_components.TrainingsSteps;
import d.shunyaev.RemoteTrainingTgBot.utils.CallServerHelper;
import d.shunyaev.RemoteTrainingTgBot.utils.CreateButtonHelper;
import d.shunyaev.model.Exercises;
import d.shunyaev.model.ResponseContainerResult;
import d.shunyaev.model.Trainings;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.*;

@Component
public class GetTrainingsComponent {

    private final TrainingsSteps getTrainingsSteps;

    public GetTrainingsComponent(
            TrainingsSteps getTrainingsSteps
    ) {
        this.getTrainingsSteps = getTrainingsSteps;
    }

    public EditMessageText setTrainingIsDone(CallbackQuery callbackQuery, long chatId, EditMessageText editMessageText) {
        if (!callbackQuery.getData().contains("getTraining/isDone/")) {
            return editMessageText;
        }
        long trainingId = Long.parseLong(
                callbackQuery.getData().replaceAll("editMessage/getTraining/isDone/", "")
        );

        ResponseContainerResult result = CallServerHelper.callRemoteTrainingApp(
                () -> getTrainingsSteps.setTrainingIsDone(chatId, trainingId));

        Trainings training = getTrainingsSteps.getTrainingsByChatId(chatId)
                .getTrainings()
                .stream()
                .filter(t -> t.getTrainingId().equals(trainingId))
                .findFirst()
                .get();
        editMessageText = createTrainingMessageEdit(training, chatId);
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        if (result.getCode() != 200) {
            editMessageText.setText(
                    editMessageText.getText() +
                            "\n Произошла ошибка, повторите попытку."
            );
        }
        return editMessageText;
    }

    public List<SendMessage> getTrainings(Message requestMessage, long chatId) {
        List<SendMessage> responseList = new ArrayList<>();

        if (!requestMessage.getText().contains("/get_my_trainings")) {
            return responseList;
        }
        List<Trainings> trainings = getTrainingsSteps.getTrainingsByChatId(chatId)
                .getTrainings();

        if (Objects.isNull(trainings) || trainings.isEmpty()) {
            SendMessage responseMessage = new SendMessage();
            responseMessage.setChatId(chatId);

            responseMessage.setText("У вас нет тренировок");
            responseMessage.setReplyMarkup(
                    CreateButtonHelper.addMarkupButton(
                            "Добавить первое упражнение ",
                            CREATE_NEW_TRAINING.getUrl()
                    )
            );

            return List.of(responseMessage);
        }

        for (Trainings training : trainings) {
            SendMessage responseMessage = createTrainingMessage(training, chatId);
            responseList.add(responseMessage);
        }
        return responseList;
    }

    public EditMessageText createTrainingMessageEdit(
            Trainings training,
            long chatId
    ) {
        return (EditMessageText) createTrainingMessage(null, new EditMessageText(), training, chatId);
    }

    private SendMessage createTrainingMessage(
            Trainings training,
            long chatId
    ) {
        return (SendMessage) createTrainingMessage(new SendMessage(), null, training, chatId);
    }

    private Object createTrainingMessage(
            SendMessage responseMessage,
            EditMessageText editMessageText,
            Trainings training,
            long chatId
    ) {
        StringBuilder text = new StringBuilder("🏋️‍♂️ *Дата тренировки:* %s\n".formatted(training.getDate()) +
                "📅 *День недели:* %s\n".formatted(training.getDayOfWeek()) +
                "💪 *Тренировка:* %s\n".formatted(training.getMuscleGroup()));
        text.append("------------------------------------------\n");
        int count = 1;

        if (Objects.isNull(training.getExercises()) || training.getExercises().isEmpty()) {
            text.append("\t Упражнения не были добавлены.\n")
                    .append("------------------------------------------\n");
        } else {
            for (Exercises exercise : training.getExercises()) {
                text.append("   *№ %s :* *%s*\n".formatted(count, exercise.getExerciseName()))
                        .append("   - Повторения: *%s*\n".formatted(exercise.getQuantity()))
                        .append("   - Подходы: *%s*\n".formatted(exercise.getApproach()))
                        .append("   - Вес: *%s*\n".formatted(exercise.getWeight()))
                        .append("------------------------------------------\n");

                count++;
            }
        }

        text.append("%s *Статус тренировки:* %s"
                .formatted(Boolean.TRUE.equals(training.getIsDone())
                                ? "✅"
                                : "❌",
                        Boolean.TRUE.equals(training.getIsDone())
                                ? "Выполнена"
                                : "Не выполнена"));

        if (Objects.nonNull(responseMessage)) {
            responseMessage.setChatId(chatId);
            responseMessage.setText(text.toString());
            responseMessage.enableMarkdown(true);
            responseMessage.setReplyMarkup(buildReplyMarkup(
                    training.getIsDone(),
                    training.getTrainingId(),
                    training.getExercises()
            ));
            return responseMessage;
        } else {
            editMessageText.setChatId(chatId);
            editMessageText.setText(text.toString());
            editMessageText.enableMarkdown(true);
            editMessageText.setReplyMarkup(buildReplyMarkup(
                    training.getIsDone(),
                    training.getTrainingId(),
                    training.getExercises()
            ));
            return editMessageText;
        }
    }

    private InlineKeyboardMarkup buildReplyMarkup(Boolean trainingIsDone, Long trainingId,
                                                  List<Exercises> exercisesList) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (Objects.isNull(exercisesList) || exercisesList.isEmpty()) {
            keyboard.add(
                    CreateButtonHelper.createButtonList(
                            "Добавить первое упражнение ",
                            CREATE_NEW_EXERCISE.getUrl() + trainingId
                    )
            );
            markup.setKeyboard(keyboard);
        } else if (!trainingIsDone) {
            keyboard.add(
                    CreateButtonHelper.createButtonList(
                            "✅ Тренировка выполнена",
                            "editMessage/getTraining/isDone/%s"
                                    .formatted(trainingId)
                    )
            );
        }
        keyboard.add(
                CreateButtonHelper.createButtonList(
                        "Изменить тренировку",
                        UPDATE_TRAINING.getUrl() + "%s"
                                .formatted(trainingId)
                )
        );
        markup.setKeyboard(keyboard);

        return markup;
    }
}