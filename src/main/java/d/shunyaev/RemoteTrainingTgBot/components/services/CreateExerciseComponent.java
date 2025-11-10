package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.BuildGridComponent;
import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.TrainingsSteps;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.BadRequestException;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.enums.Exercises;
import d.shunyaev.RemoteTrainingTgBot.enums.MuscleGroup;
import d.shunyaev.model.RequestContainerCreateExerciseRequest;
import d.shunyaev.model.ResponseContainerResult;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.CREATE_NEW_EXERCISE;

@Component
public class CreateExerciseComponent {

    private final TrainingsSteps getTrainingsComponent;
    private final BuildGridComponent buildGridComponent;

    public CreateExerciseComponent(TrainingsSteps getTrainingsComponent, BuildGridComponent buildGridComponent) {
        this.getTrainingsComponent = getTrainingsComponent;
        this.buildGridComponent = buildGridComponent;
    }

    public SendMessage createExercise(CallbackQuery callbackQuery, long chatId) {
        RequestContainerCreateExerciseRequest req = getExerciseRequest(chatId);
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);

        String data = Optional.ofNullable(callbackQuery)
                .map(CallbackQuery::getData)
                .map(d -> d.replaceAll(CREATE_NEW_EXERCISE.getUrl(), ""))
                .orElse("");

        String regexRange = "\\d*-\\d*";
        String regexValue = "\\d*";

        if (data.matches(regexValue)) {
            req.setTrainingId(Long.parseLong(data));
            return addExerciseName(responseMessage, chatId);
        } else if (data.matches(buildGridComponent.quantityCallback + regexRange)) {
            return chooseQuantity(responseMessage, data);
        } else if (data.matches(buildGridComponent.quantityCallback + regexValue)) {
            req.setQuantity(Integer.parseInt(data.replaceAll(buildGridComponent.quantityCallback, "")));
            return chooseWeight(responseMessage, data);
        } else if (data.matches(buildGridComponent.weightCallback + regexRange)) {
            return chooseWeight(responseMessage, data);
        } else if (data.matches(buildGridComponent.weightCallback + regexValue)) {
            req.setWeight(Integer.parseInt(data.replaceAll(buildGridComponent.weightCallback, "")));
            return chooseApproach(responseMessage, data);
        } else if (data.matches(buildGridComponent.approachCallback + regexValue)) {
            req.setApproach(Integer.parseInt(data.replaceAll(buildGridComponent.approachCallback, "")));
            return callSetNewExercise(responseMessage, req, chatId);
        } else if (isValidExerciseName(data)) {
            req.setExerciseName(Exercises.valueOf(data).getDescription());
            return chooseQuantity(responseMessage, data);
        }

        return responseMessage;
    }

    private boolean isValidExerciseName(String data) {
        try {
            return Arrays.stream(Exercises.values())
                    .collect(Collectors.toSet())
                    .contains(Exercises.valueOf(data));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private SendMessage chooseApproach(SendMessage responseMessage, String data) {
        responseMessage.setText("Выберите количество подходов:");
        responseMessage.setReplyMarkup(buildGridComponent
                .buildGrid(data, buildGridComponent.approachCallback, IntStream.rangeClosed(1, 10)
                .mapToObj(String::valueOf)
                .toList(), CREATE_NEW_EXERCISE.getUrl()));
        return responseMessage;
    }

    private SendMessage chooseWeight(SendMessage responseMessage, String data) {
        responseMessage.setText("Выберите вес снаряжения:");
        responseMessage.setReplyMarkup(buildGridComponent.buildGrid(data, buildGridComponent.weightCallback,
                List.of("0-50", "51-100", "101-150", "151-200", "201-250", "251-300"), CREATE_NEW_EXERCISE.getUrl()));
        return responseMessage;
    }

    private SendMessage chooseQuantity(SendMessage responseMessage, String data) {
        responseMessage.setText("Выберите количество повторений:");
        responseMessage.setReplyMarkup(buildGridComponent.buildGrid(data, buildGridComponent.quantityCallback,
                List.of("1-10", "11-20", "21-30"), CREATE_NEW_EXERCISE.getUrl()));
        return responseMessage;
    }

    private SendMessage addExerciseName(SendMessage responseMessage, long chatId) {
        RequestContainerCreateExerciseRequest req = getExerciseRequest(chatId);

        var training = getTrainingsComponent.getTrainingsByChatId(chatId)
                .getTrainings()
                .stream()
                .filter(tr -> tr.getTrainingId().equals(req.getTrainingId()))
                .findFirst()
                .orElseThrow();
        String text;

        if (training.getMuscleGroup().contains(MuscleGroup.OTHER.getDescription())) {
            text = "Введите название упражнения:";
        } else {
            text = "Выберите упражнение:";
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            var muscleGroup = Arrays.stream(training.getMuscleGroup().split(" \\+ "))
                    .map(MuscleGroup::getByDescription)
                    .collect(Collectors.toSet());

            var exercises = Arrays.stream(Exercises.values())
                    .filter(exerc -> muscleGroup.contains(exerc.getMuscleGroup()))
                    .toList();

            for (Exercises exercise : exercises) {
                InlineKeyboardButton b = new InlineKeyboardButton();
                b.setText(exercise.getDescription());
                b.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + exercise.name());
                keyboard.add(List.of(b));
            }

            markup.setKeyboard(keyboard);
            responseMessage.setReplyMarkup(markup);
        }
        responseMessage.setText(text);
        return responseMessage;
    }

    private SendMessage callSetNewExercise(SendMessage responseMessage,
                                           RequestContainerCreateExerciseRequest request,
                                           long chatId) {
        if (Objects.nonNull(request.getTrainingId()) &&
                Objects.nonNull(request.getWeight()) &&
                Objects.nonNull(request.getApproach()) &&
                Objects.nonNull(request.getExerciseName()) &&
                Objects.nonNull(request.getQuantity())) {

            ResponseContainerResult result;
            try {
                result = RemoteAppController.getExerciseControllerApi().createExercise(request);
            } catch (BadRequestException e) {
                result = e.getResponseBody();
            }

            assert result.getCode() != null;
            int code = result.getCode();

            if (code == 200) {
                CashComponent.CREATE_EXERCISE_REQUEST.remove(chatId);
                responseMessage.setText("Упражнение успешно добавлено");

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                InlineKeyboardButton next = new InlineKeyboardButton();
                next.setText("Добавить следующее упражнение");
                next.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + request.getTrainingId());
                keyboard.add(List.of(next));

                InlineKeyboardButton done = new InlineKeyboardButton();
                done.setText("Завершить");
                done.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + "done");
                keyboard.add(List.of(done));

                markup.setKeyboard(keyboard);
                responseMessage.setReplyMarkup(markup);

            } else if (code == -8) {
                assert result.getMessage() != null;
                responseMessage.setText(result.getMessage());

            } else {
                responseMessage.setText("Ошибка добавления упражнения: \n" +
                        result.getMessage() + "\n Повторите попытку:");

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                InlineKeyboardButton add = new InlineKeyboardButton();
                add.setText("Добавить упражнение");
                add.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + request.getTrainingId());

                keyboard.add(List.of(add));
                markup.setKeyboard(keyboard);
                responseMessage.setReplyMarkup(markup);

                CashComponent.CREATE_EXERCISE_REQUEST.put(chatId, new RequestContainerCreateExerciseRequest());
            }
        } else {
            responseMessage.setText("Ошибка добавления упражнения:\n Повторите попытку:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            InlineKeyboardButton add = new InlineKeyboardButton();
            add.setText("Добавить упражнение");
            keyboard.add(List.of(add));

            markup.setKeyboard(keyboard);
            responseMessage.setReplyMarkup(markup);

            CashComponent.CREATE_EXERCISE_REQUEST.put(chatId, new RequestContainerCreateExerciseRequest());
        }

        return responseMessage;
    }

    private RequestContainerCreateExerciseRequest getExerciseRequest(long chatId) {
        return CashComponent.CREATE_EXERCISE_REQUEST.computeIfAbsent(
                chatId, k -> new RequestContainerCreateExerciseRequest());
    }
}