package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.BuildGridComponent;
import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.TrainingsSteps;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.BadRequestException;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl;
import d.shunyaev.RemoteTrainingTgBot.utils.ConvertedUtils;
import d.shunyaev.model.*;
import d.shunyaev.model.RequestContainerUpdateTrainingRequest.DayOfWeekEnum;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.UPDATE_TRAINING;

@Component
public class UpdateTrainingsComponent {

    private final TrainingsSteps trainingsSteps;
    private final GetTrainingsComponent getTrainingsComponent;
    private final BuildGridComponent buildGridComponent;
    private final Map<Long, Long> trainingsIds = new HashMap<>();
    private final Map<Long, Long> exerciseIds = new HashMap<>();

    public UpdateTrainingsComponent(TrainingsSteps trainingsSteps, GetTrainingsComponent getTrainingsComponent, BuildGridComponent buildGridComponent) {
        this.trainingsSteps = trainingsSteps;
        this.getTrainingsComponent = getTrainingsComponent;
        this.buildGridComponent = buildGridComponent;
    }

    public EditMessageText updateTraining(CallbackQuery callbackQuery, long chatId, EditMessageText editMessageText) {
        String data = callbackQuery.getData();
        if (!data.contains(UPDATE_TRAINING.getUrl())) {
            return editMessageText;
        }
        if (data.contains("updateExercise") && !data.contains("updateExercises")) {
            return updateExercise(data, chatId, editMessageText);
        }

        TrainingIdAndNewDateTraining trainingIdAndNewDateTraining = getTrainingIdAndNewDateTraining(data);
        Long trainingId = trainingIdAndNewDateTraining.trainingId;
        String newDateTraining = trainingIdAndNewDateTraining.newDataTraining;

        data = data
                .replaceAll(UPDATE_TRAINING.getUrl(), "")
                .replaceAll("\\d.*", "%s");

        VariablesChangeTraining dataVariables = VariablesChangeTraining.getByUrl(data);

        switch (dataVariables) {
            case UPDATE_TRAINING -> updateTraining(trainingId, editMessageText);
            case UPDATE_EXERCISES -> chooseDeleteOrUpdateExercises(chatId, trainingId,
                    editMessageText, VariablesChangeTraining.UPDATE_EXERCISE);
            case DELETE_TRAINING -> {
                if (Objects.isNull(trainingId)) {
                    deleteExerciseOrTraining(chatId, editMessageText);
                } else {
                    deleteTraining(trainingId, editMessageText);
                }
            }
            case DELETE_EXERCISES -> chooseDeleteOrUpdateExercises(chatId, trainingId,
                    editMessageText, VariablesChangeTraining.DELETE_EXERCISE);
            case DELETE_EXERCISE -> deleteExercise(chatId, trainingId, editMessageText);
            default -> defaultChange(data, newDateTraining, chatId, trainingId, editMessageText);
        }

        return editMessageText;
    }

    private void defaultChange(String data, String newDateTraining, long chatId,
                               Long trainingId, EditMessageText editMessageText) {
        if (data.contains("changeDateOfTraining")) {
            if (Objects.isNull(newDateTraining)) {
                changeDayOfTraining(chatId, trainingId, editMessageText);
            } else {
                updateTraining(newDateTraining, chatId, trainingId, editMessageText);
            }
        } else {
            chooseVariables(chatId, trainingId, editMessageText);
        }
    }

    private void deleteExerciseOrTraining(long chatId, EditMessageText editMessageText) {
        long trainingId = trainingsIds.get(chatId);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        trainingsIds.put(chatId, trainingId);

        for (VariablesChangeTraining var : List.of(
                VariablesChangeTraining.DELETE_TRAINING,
                VariablesChangeTraining.DELETE_EXERCISES
        )) {
            keyboard.add(
                    createButton(
                            var.getDescription(),
                            var.getUrl().formatted(trainingId)
                    )
            );
        }
        keyboard.add(createBackButton());

        markup.setKeyboard(keyboard);
        editMessageText.setText("Выберете подходящее");
        editMessageText.setReplyMarkup(markup);
    }

    private EditMessageText updateExercise(String data, long chatId, EditMessageText editMessageText) {
        if (data.contains("done")) {
            return updateExercise(chatId, editMessageText);
        }
        String url = "updateTraining/updateExercise/";
        long value = Long.parseLong(data.replaceAll("\\D", ""));
        if (data.matches(url + "\\d*")) {
            exerciseIds.put(chatId,
                    Long.valueOf(data.replaceAll(url, ""))
            );
        }
        data = data.contains("choose")
                ? data
                    .replaceAll(url, "")
                    .replaceAll("choose/", "")
                    .replaceAll("\\d*", "")
                : data
                    .replaceAll(url, "");

        EditMessageText variable = setParamsInUpdateExerciseRequest(data, chatId, editMessageText);
        if (Objects.nonNull(variable)) {
            return variable;
        }

        if (data.contains(buildGridComponent.weightCallback)) {
            data = data.equals(buildGridComponent.weightCallback)
                    ? ""
                    : data;
            editMessageText.setText("Выберите вес снаряжения:");
            editMessageText.setReplyMarkup(buildGridComponent.buildGrid(data, buildGridComponent.weightCallback,
                    List.of("0-50", "51-100", "101-150", "151-200", "201-250", "251-300"), VariablesChangeTraining.UPDATE_EXERCISE.getUrl()
                            .formatted("")));
            return editMessageText;
        } else if (data.contains(buildGridComponent.quantityCallback)) {
            data = data.equals(buildGridComponent.quantityCallback)
                    ? ""
                    : data;
            editMessageText.setText("Выберите количество повторений:");
            editMessageText.setReplyMarkup(buildGridComponent.buildGrid(data, buildGridComponent.quantityCallback,
                    List.of("1-10", "11-20", "21-30"), VariablesChangeTraining.UPDATE_EXERCISE.getUrl()
                            .formatted("")));
            return editMessageText;
        } else if (data.contains(buildGridComponent.approachCallback)) {
            data = data.equals(buildGridComponent.approachCallback)
                    ? ""
                    : data;
            editMessageText.setText("Выберите количество подходов:");
            editMessageText.setReplyMarkup(buildGridComponent
                    .buildGrid(data, buildGridComponent.approachCallback, IntStream.rangeClosed(1, 10)
                                    .mapToObj(String::valueOf).toList(),
                            VariablesChangeTraining.UPDATE_EXERCISE.getUrl()
                                    .formatted("")));
            return editMessageText;
        }

        return chooseExerciseUpdateValue(editMessageText, value, chatId);
    }

    private EditMessageText setParamsInUpdateExerciseRequest(String data, long chatId, EditMessageText editMessageText) {
        RequestContainerUpdateExerciseRequest request = CashComponent.UPDATE_EXERCISE_REQUEST.computeIfAbsent(
                chatId, k -> new RequestContainerUpdateExerciseRequest());
        if (!data.matches("[a-z]+/\\d+")) {
            return null;
        }
        int value = Integer.parseInt(data.replaceAll("\\D", ""));
        if (data.matches(buildGridComponent.weightCallback + "\\d*")) {
            request.setWeight(value);
        } else if (data.matches(buildGridComponent.quantityCallback + "\\d*")) {
            request.setQuantity(value);

        } else {
            request.setApproach(value);
        }
        long exerciseId = exerciseIds.get(chatId);
        return chooseExerciseUpdateValue(editMessageText, exerciseId, chatId);
    }

    private EditMessageText chooseExerciseUpdateValue(EditMessageText editMessageText, long exerciseId, long chatId) {
        RequestContainerUpdateExerciseRequest request = CashComponent.UPDATE_EXERCISE_REQUEST.computeIfAbsent(
                chatId, k -> new RequestContainerUpdateExerciseRequest());
        editMessageText.setText("Выберете параметр:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (Objects.isNull(request.getWeight())) {
            keyboard.add(
                    createButton(
                            "Изменить вес",
                            VariablesChangeTraining.UPDATE_EXERCISE.getUrl()
                                    .formatted("choose/" + buildGridComponent.weightCallback) + exerciseId
                    )
            );
        }
        if (Objects.isNull(request.getQuantity())) {
            keyboard.add(
                    createButton(
                            "Изменить количество повторений",
                            VariablesChangeTraining.UPDATE_EXERCISE.getUrl()
                                    .formatted("choose/" + buildGridComponent.quantityCallback) + exerciseId
                    )
            );
        }
        if (Objects.isNull(request.getApproach())) {
            keyboard.add(
                    createButton(
                            "Изменить количество подходов",
                            VariablesChangeTraining.UPDATE_EXERCISE.getUrl()
                                    .formatted("choose/" + buildGridComponent.approachCallback) + exerciseId
                    )
            );
        }

        keyboard.add(
                createButton(
                        "Завершить",
                        VariablesChangeTraining.UPDATE_EXERCISE.getUrl()
                                .formatted("choose/done/") + exerciseId
                )
        );

        keyboard.add(createBackButton());

        markup.setKeyboard(keyboard);
        editMessageText.setReplyMarkup(markup);
        return editMessageText;
    }

    private EditMessageText updateExercise(long chatId, EditMessageText editMessageText) {
        long exerciseId = exerciseIds.get(chatId);
        RequestContainerUpdateExerciseRequest request = CashComponent.UPDATE_EXERCISE_REQUEST.get(chatId)
                .exerciseId(exerciseId);

        ResponseContainerResult result;
        try {
            result = RemoteAppController.getExerciseControllerApi().updateExercise(request);
        } catch (BadRequestException e) {
            result = e.getResponseBody();
        }

        if (result.getCode() == 200) {
            CashComponent.UPDATE_EXERCISE_REQUEST.remove(chatId);
            long trainingId = trainingsIds.get(chatId);
            Trainings training = trainingsSteps.getTrainingByTrainingId(chatId, trainingId);
            EditMessageText newEditMessage = getTrainingsComponent.createTrainingMessageEdit(training, chatId);
            newEditMessage.setMessageId(editMessageText.getMessageId());
            trainingsIds.remove(chatId);
            exerciseIds.remove(chatId);
            return newEditMessage;
        } else {
            editMessageText.setText("Ошибка обновления данных: %s"
                    .formatted(result.getMessage()));
        }
        return editMessageText;
    }

    private void updateTraining(String newDateTraining, long chatId, long trainingId, EditMessageText editMessageText) {
        LocalDate newDate = LocalDate.parse(newDateTraining, DateTimeFormatter.ISO_DATE);
        DayOfWeekEnum dayOfWeek = Arrays.stream(DayOfWeekEnum.values())
                .filter(day -> day.getValue().equals(
                        ConvertedUtils.convertToRusDayOfWeek(newDate.getDayOfWeek())
                ))
                .findFirst()
                .get();

        RequestContainerUpdateTrainingRequest request = new RequestContainerUpdateTrainingRequest()
                .date(newDateTraining)
                .dayOfWeek(dayOfWeek)
                .trainingId(trainingId);

        ResponseContainerResult result;
        try {
            result = RemoteAppController.getTrainingControllerApi().updateTraining(request);
        } catch (BadRequestException e) {
            result = e.getResponseBody();
        }

        if (result.getCode() == 200) {
            Trainings training = trainingsSteps.getTrainingByTrainingId(chatId, trainingId);
            EditMessageText newEditMessage = getTrainingsComponent.createTrainingMessageEdit(training, chatId);

            editMessageText.setReplyMarkup(newEditMessage.getReplyMarkup());
            editMessageText.setText(newEditMessage.getText());
        } else {
            editMessageText.setText("Ошибка обновления данных: %s"
                    .formatted(result.getMessage()));
        }
    }

    private void changeDayOfTraining(long chatId, long trainingId, EditMessageText editMessageText) {
        Trainings training = trainingsSteps.getTrainingByTrainingId(chatId, trainingId);
        List<LocalDate> options = getDaysFromTraining(
                LocalDate.parse(training.getDate(), DateTimeFormatter.ISO_DATE));

        editMessageText.setText("Выберете новый день тренировки:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (LocalDate d : options) {
            keyboard.add(
                    createButton(
                            d.getDayOfMonth() + " " + ConvertedUtils.convertMonthToRussian(d),
                            UPDATE_TRAINING.getUrl() + "changeDateOfTraining/%s/%s"
                                    .formatted(trainingId, d)
                    )
            );
        }
        keyboard.add(createBackButton());

        markup.setKeyboard(keyboard);
        editMessageText.setReplyMarkup(markup);
    }

    private void deleteExercise(long chatId, long exerciseId, EditMessageText editMessageText) {
        ResponseContainerResult result;
        try {
            result = RemoteAppController.getExerciseControllerApi().deleteExercise(
                    new RequestContainerDeleteExerciseRequest()
                            .exerciseId(exerciseId)
            );
        } catch (BadRequestException e) {
            result = e.getResponseBody();
        }
        if (result.getCode() == 200) {
            long trainingId = trainingsIds.get(chatId);
            Trainings training = trainingsSteps.getTrainingByTrainingId(chatId, trainingId);
            EditMessageText newEditMessage = getTrainingsComponent.createTrainingMessageEdit(training, chatId);

            editMessageText.setText(newEditMessage.getText());
            editMessageText.setReplyMarkup(newEditMessage.getReplyMarkup());

            trainingsIds.remove(chatId);
        } else {
            editMessageText.setText("Ошибка удаления упражнения: %s"
                    .formatted(result.getMessage()));
        }
    }

    private void chooseDeleteOrUpdateExercises(long chatId, long trainingId,
                                               EditMessageText editMessageText,
                                               VariablesChangeTraining variablesChangeTraining) {
        List<Exercises> exercises = Objects.requireNonNull(trainingsSteps.getTrainingByTrainingId(chatId, trainingId)
                .getExercises());

        trainingsIds.put(chatId, trainingId);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Exercises exercise : exercises) {
            keyboard.add(
                    createButton(
                            exercise.getExerciseName(),
                            variablesChangeTraining.getUrl().formatted(exercise.getExerciseId())
                    )
            );
        }
        keyboard.add(createBackButton());

        markup.setKeyboard(keyboard);
        editMessageText.setText("Выберете подходящее");
        editMessageText.setReplyMarkup(markup);
    }

    private void deleteTraining(long trainingId, EditMessageText editMessageText) {
        ResponseContainerResult result;
        try {
            result = RemoteAppController.getTrainingControllerApi().deleteTraining(
                    new RequestContainerDeleteTrainingRequest()
                            .trainingId(trainingId)
            );
        } catch (BadRequestException e) {
            result = e.getResponseBody();
        }
        if (result.getCode() == 200) {
            editMessageText.setText("Тренировка удалена.");
        } else {
            editMessageText.setText("Ошибка удаления тренировки: %s"
                    .formatted(result.getMessage()));
        }
    }

    private void updateTraining(long trainingId, EditMessageText editMessageText) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(
                createButton(
                        "Изменить дату тренировки",
                        UPDATE_TRAINING.getUrl() + "changeDateOfTraining/%s"
                                .formatted(trainingId)
                )
        );

        VariablesChangeTraining var = VariablesChangeTraining.UPDATE_EXERCISES;
        keyboard.add(
                createButton(
                        var.getDescription(),
                        var.getUrl()
                                .formatted(trainingId)
                )
        );

        keyboard.add(createBackButton());

        markup.setKeyboard(keyboard);
        editMessageText.setText("Выберете подходящее");
        editMessageText.setReplyMarkup(markup);
    }

    private void chooseVariables(long chatId, long trainingId, EditMessageText editMessageText) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        trainingsIds.put(chatId, trainingId);

        for (VariablesChangeTraining var : List.of(
                VariablesChangeTraining.UPDATE_TRAINING,
                VariablesChangeTraining.DELETE_TRAINING
        )) {
            keyboard.add(
                    createButton(
                            var.getDescription(),
                            var.equals(VariablesChangeTraining.UPDATE_TRAINING)
                                    ? var.getUrl().formatted(trainingId)
                                    : var.getUrl()
                    )
            );
        }
        keyboard.add(createBackButton());

        markup.setKeyboard(keyboard);
        editMessageText.setText("Выберете подходящее");
        editMessageText.setReplyMarkup(markup);
    }

    private List<LocalDate> getDaysFromTraining(LocalDate dateTrainingNow) {
        LocalDate date = LocalDate.now();
        YearMonth ym = YearMonth.from(date);
        List<LocalDate> days = getDaysForMonth(ym, date.getDayOfMonth(), dateTrainingNow);
        return days.isEmpty() ? getDaysForMonth(ym.plusMonths(1), 1, dateTrainingNow) : days;
    }

    private List<LocalDate> getDaysForMonth(YearMonth yearMonth, int startDay, LocalDate dateTrainingNow) {
        return IntStream.rangeClosed(startDay, yearMonth.lengthOfMonth())
                .mapToObj(d -> LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), d))
                .filter(f -> !f.equals(dateTrainingNow))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<InlineKeyboardButton> createBackButton() {
        return createButton("Назад ⏪", "back");
    }

    private List<InlineKeyboardButton> createButton(String description, String callbackData) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(description);
        b.setCallbackData(callbackData);
        row.add(b);
        return row;
    }

    private TrainingIdAndNewDateTraining getTrainingIdAndNewDateTraining(String data) {
        String[] dataMass = data.split("/");
        Long trainingId = Long.parseLong(
                Arrays.stream(dataMass)
                        .filter(i -> i.matches("\\d.*"))
                        .findFirst()
                        .orElse("-1")
        );
        trainingId = trainingId >= 0
                ? trainingId
                : null;

        String newDateTraining = Arrays.stream(dataMass)
                .filter(i -> i.matches("\\d{4}-\\d{2}-\\d{2}"))
                .findFirst()
                .orElse(null);
        return new TrainingIdAndNewDateTraining(trainingId, newDateTraining);
    }

    private enum VariablesChangeTraining {
        UPDATE_TRAINING("updateTrainings/%s", "Изменить тренировку"),
        UPDATE_EXERCISES("updateExercises/%s", "Изменить упражнения"),
        UPDATE_EXERCISE("updateExercise/%s", "Изменить упражнения"),
        DELETE_TRAINING("deleteTraining/%s", "Удалить тренировку"),
        DELETE_EXERCISES("deleteExercises/%s", "Удалить упражнения"),
        DELETE_EXERCISE("deleteExercise/%s", "Удалить упражнения"),
        NON("", "");

        private final String url;

        public String getDescription() {
            return description;
        }

        private final String description;

        public String getUrl() {
            return ServicesUrl.UPDATE_TRAINING.getUrl() + this.url;
        }

        VariablesChangeTraining(String url, String description) {
            this.url = url;
            this.description = description;
        }

        public static VariablesChangeTraining getByUrl(@NotNull String url) {
            return Arrays.stream(VariablesChangeTraining.values())
                    .filter(var -> var.url.equals(url))
                    .findFirst()
                    .orElse(NON);
        }
    }

    private record TrainingIdAndNewDateTraining(Long trainingId, String newDataTraining) {
    }
}
