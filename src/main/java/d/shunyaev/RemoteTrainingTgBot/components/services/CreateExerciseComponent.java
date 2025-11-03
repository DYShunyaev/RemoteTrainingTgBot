package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetTrainingsComponent;
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

    private final GetTrainingsComponent getTrainingsComponent;
    private final String quantityCallback = "quantity/";
    private final String approachCallback = "approach/";
    private final String weightCallback = "weight/";


    public CreateExerciseComponent(
            GetTrainingsComponent getTrainingsComponent
    ) {
        this.getTrainingsComponent = getTrainingsComponent;
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
        } else if (data.matches(quantityCallback + regexRange)) {
            return chooseQuantity(responseMessage, data);
        } else if (data.matches(quantityCallback + regexValue)) {
            req.setQuantity(
                    Integer.parseInt(data.replaceAll(quantityCallback, ""))
            );
            return chooseWeight(responseMessage, data);
        } else if (data.matches(weightCallback + regexRange)) {
            return chooseWeight(responseMessage, data);
        } else if (data.matches(weightCallback + regexValue)) {
            req.setWeight(
                    Integer.parseInt(data.replaceAll(weightCallback, ""))
            );
            return chooseApproach(responseMessage, data);
        } else if (data.matches(approachCallback + regexValue)) {
            req.setApproach(
                    Integer.parseInt(data.replaceAll(approachCallback, ""))
            );
            return callSetNewExercise(responseMessage, req, chatId);
        } else if (
                Arrays.stream(Exercises.values())
                        .collect(Collectors.toSet()).contains(Exercises.valueOf(data))
        ) {
            req.setExerciseName(Exercises.valueOf(data).getDescription());
            return chooseQuantity(responseMessage, data);
        }

        return responseMessage;
    }

    private SendMessage chooseApproach(SendMessage responseMessage, String data) {
        responseMessage.setText("Выберете колличество подходов:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(
                setBoundaryValues(data, approachCallback,
                        IntStream.range(1, 20)
                                .mapToObj(String::valueOf)
                                .toList())
        );
        responseMessage.setReplyMarkup(markup);

        return responseMessage;
    }

    private SendMessage chooseWeight(SendMessage responseMessage, String data) {
        responseMessage.setText("Выберете вес снаряжения:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(
                setBoundaryValues(data, weightCallback,
                        List.of("0-50", "51-100", "101-150", "151-200", "201-250", "251-300"))
        );
        responseMessage.setReplyMarkup(markup);

        return responseMessage;
    }

    private SendMessage chooseQuantity(SendMessage responseMessage, String data) {
        responseMessage.setText("Выберете колличество повторений:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(
                setBoundaryValues(data, quantityCallback, List.of("1-10", "11-20", "21-30"))
        );
        responseMessage.setReplyMarkup(markup);

        return responseMessage;
    }

    private SendMessage addExerciseName(SendMessage responseMessage, long chatId) {
        RequestContainerCreateExerciseRequest req = getExerciseRequest(chatId);

        responseMessage.setText("Выберете упражнение:");
        var training = getTrainingsComponent.getTrainingsByChatId(chatId)
                .getTrainings()
                .stream()
                .filter(tr -> tr.getTrainingId().equals(req.getTrainingId()))
                .findFirst()
                .get();

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
        return responseMessage;
    }

    private List<List<InlineKeyboardButton>> setBoundaryValues(String data, String callBack, List<String> boundary) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> keys = new ArrayList<>();
        if (data.contains(callBack)) {
            var dataRepl = Arrays.stream(data.replaceAll(callBack, "")
                            .split("-"))
                    .map(Integer::valueOf)
                    .sorted()
                    .toList();

            for (int i = dataRepl.get(0); i <= dataRepl.get(1); i++) {
                InlineKeyboardButton b = new InlineKeyboardButton();
                b.setText(String.valueOf(i));
                b.setCallbackData(
                        CREATE_NEW_EXERCISE.getUrl()
                                + callBack
                                + i);
                keys.add(b);
                if (keys.size() == 3) {
                    keyboard.add(keys);
                    keys = new ArrayList<>();
                }
            }
        } else {
            for (String str : boundary) {
                InlineKeyboardButton b = new InlineKeyboardButton();
                b.setText(str);
                b.setCallbackData(
                        CREATE_NEW_EXERCISE.getUrl()
                                + callBack
                                + str);
                keys.add(b);
                if (keys.size() == 3) {
                    keyboard.add(keys);
                    keys = new ArrayList<>();
                }
            }
        }
        if (keys.size() > 0) {
            keyboard.add(keys);
        }
        return keyboard;
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
            if (result.getCode().equals(200)) {
                CashComponent.CREATE_EXERCISE_REQUEST.remove(chatId);
                responseMessage.setText("Упражнение успешно добавлена");

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                InlineKeyboardButton first = new InlineKeyboardButton();
                first.setText("Добавить следующее упражнение");
                first.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + request.getTrainingId());
                keyboard.add(List.of(first));

                InlineKeyboardButton second = new InlineKeyboardButton();
                second.setText("Завершить");
                second.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + "done");
                keyboard.add(List.of(second));

                markup.setKeyboard(keyboard);
                responseMessage.setReplyMarkup(markup);

            } else if (result.getCode().equals(-8)) {
                assert result.getMessage() != null;
                responseMessage.setText(result.getMessage());

            } else {
                responseMessage.setText("Ошибка добавления упражнения: \n" +
                        result.getMessage() + "\n Повторите попытку:");
                CashComponent.CREATE_EXERCISE_REQUEST.put(chatId, new RequestContainerCreateExerciseRequest());
            }
        } else {
            responseMessage.setText("Ошибка добавления упражнения:\n Повторите попытку:");
            CashComponent.CREATE_EXERCISE_REQUEST.put(chatId, new RequestContainerCreateExerciseRequest());
        }

        return responseMessage;
    }

    private RequestContainerCreateExerciseRequest getExerciseRequest(long chatId) {
        return CashComponent.CREATE_EXERCISE_REQUEST.computeIfAbsent(
                chatId, k -> new RequestContainerCreateExerciseRequest());
    }
}
