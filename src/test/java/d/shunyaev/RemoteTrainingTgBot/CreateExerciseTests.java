package d.shunyaev.RemoteTrainingTgBot;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetTrainingsComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.CreateExerciseComponent;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.enums.Exercises;
import d.shunyaev.RemoteTrainingTgBot.enums.MuscleGroup;
import d.shunyaev.RemoteTrainingTgBot.utils.RandomUtils;
import d.shunyaev.api.ExerciseControllerApi;
import d.shunyaev.model.RequestContainerCreateExerciseRequest;
import d.shunyaev.model.ResponseContainerGetTrainingsResponse;
import d.shunyaev.model.ResponseContainerResult;
import d.shunyaev.model.Trainings;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.CREATE_NEW_EXERCISE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateExerciseTests {

    @InjectMocks
    private CreateExerciseComponent createExerciseComponent;
    @Mock
    private GetTrainingsComponent getTrainingsComponent;
    private CallbackQuery callbackQuery;
    private long chatId;

    @BeforeEach
    public void setUp() {
        chatId = RandomUtils.generateRandomChatId();
        var request = new RequestContainerCreateExerciseRequest();
        CashComponent.CREATE_EXERCISE_REQUEST.put(chatId, request);

        var user = new User();
        user.setId(chatId);
        user.setUserName("user_name");
        user.setFirstName("first_name");
        user.setLastName("last_name");

        callbackQuery = new CallbackQuery();
        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl());
        callbackQuery.setFrom(user);

    }

    private static Stream<String> muscleGroups() {
        var muscleGroupsArray = MuscleGroup.values();

        StringBuilder builder = new StringBuilder();
        List<String> stringList = new ArrayList<>();

        for (MuscleGroup muscleGroup : muscleGroupsArray) {
            if (muscleGroup.equals(MuscleGroup.OTHER)) {
                continue;
            }
            if (builder.length() == 0) {
                builder.append(muscleGroup.getDescription());
                stringList.add(builder.toString());
            } else {
                builder.append(" + ").append(muscleGroup.getDescription());
                stringList.add(builder.toString());
            }
        }
        return stringList.stream();
    }

    private static Stream<String> quantityRange() {
        return Stream.of("1-10", "11-20", "21-30");
    }

    private static Stream<String> weightRange() {
        return Stream.of("0-50", "51-100", "101-150", "151-200", "201-250", "251-300");
    }

    private static Stream<RequestContainerCreateExerciseRequest> requestFieldsIsNull() {
        return Stream.of(
                new RequestContainerCreateExerciseRequest()
                        .exerciseName(null)
                        .trainingId(RandomUtils.generateRandomLong(1, 15))
                        .approach(3)
                        .weight(30)
                        .quantity(4),
                new RequestContainerCreateExerciseRequest()
                        .exerciseName(Exercises.BICEPS_5.getDescription())
                        .trainingId(null)
                        .approach(3)
                        .weight(30)
                        .quantity(4),
                new RequestContainerCreateExerciseRequest()
                        .exerciseName(Exercises.BICEPS_5.getDescription())
                        .trainingId(RandomUtils.generateRandomLong(1, 15))
                        .approach(3)
                        .weight(null)
                        .quantity(4),
                new RequestContainerCreateExerciseRequest()
                        .exerciseName(Exercises.BICEPS_5.getDescription())
                        .trainingId(RandomUtils.generateRandomLong(1, 15))
                        .approach(3)
                        .weight(30)
                        .quantity(null)
        );
    }

    @ParameterizedTest
    @MethodSource("muscleGroups")
    public void addExerciseNameTest(String muscleGroups) {
        var trainingId = RandomUtils.generateRandomLong(1, 20);
        Trainings trainings = new Trainings()
                .trainingId(trainingId)
                .muscleGroup(muscleGroups);
        var mockResponse = new ResponseContainerGetTrainingsResponse()
                .trainings(List.of(trainings));

        when(getTrainingsComponent.getTrainingsByChatId(anyLong())).thenReturn(mockResponse);

        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + trainingId);

        var actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);
        var actualReq = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Выберите упражнение:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        var muscleGroup = Arrays.stream(muscleGroups.split(" \\+ "))
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
        expectedMessage.setReplyMarkup(markup);

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedMessage, actualMessage),
                () -> Assertions.assertEquals(trainingId, actualReq.getTrainingId())
        );
    }

    @ParameterizedTest
    @MethodSource("muscleGroups")
    public void addExerciseNameMuscleGroupContainsOtherTest(String muscleGroups) {
        muscleGroups = muscleGroups + " + " + MuscleGroup.OTHER.getDescription();
        var trainingId = RandomUtils.generateRandomLong(1, 20);
        Trainings trainings = new Trainings()
                .trainingId(trainingId)
                .muscleGroup(muscleGroups);
        var mockResponse = new ResponseContainerGetTrainingsResponse()
                .trainings(List.of(trainings));

        when(getTrainingsComponent.getTrainingsByChatId(anyLong())).thenReturn(mockResponse);

        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + trainingId);

        var actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);
        var actualReq = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Введите название упражнения:");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedMessage, actualMessage),
                () -> Assertions.assertEquals(trainingId, actualReq.getTrainingId())
        );
    }

    @Test
    public void chooseQuantityTest() {
        String data = Arrays.stream(Exercises.values())
                .skip(RandomUtils.generateRandomLong(0, Exercises.values().length))
                .findFirst()
                .get()
                .name();

        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + data);

        var actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Выберите количество повторений:");
        expectedMessage.setReplyMarkup(buildGrid(data, getCallback("quantityCallback"),
                List.of("1-10", "11-20", "21-30")));

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @MethodSource("quantityRange")
    public void chooseQuantityRangeTest(String data) {
        String callback = getCallback("quantityCallback");
        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + callback + data);

        var actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Выберите количество повторений:");
        expectedMessage.setReplyMarkup(buildGrid(callback + data, callback, List.of("1-10", "11-20", "21-30")));

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void chooseWeightTest() {
        Integer data = (int) RandomUtils.generateRandomLong(1, 30);
        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + getCallback("quantityCallback") + data);

        var actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);
        var actualReq = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Выберите вес снаряжения:");
        expectedMessage.setReplyMarkup(buildGrid(data.toString(), getCallback("weightCallback"),
                List.of("0-50", "51-100", "101-150", "151-200", "201-250", "251-300")));

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedMessage, actualMessage),
                () -> Assertions.assertEquals(data, actualReq.getQuantity())
        );
    }

    @ParameterizedTest
    @MethodSource("weightRange")
    public void chooseWeightRangeTest(String data) {
        String callback = getCallback("weightCallback");
        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + callback + data);

        var actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Выберите вес снаряжения:");
        expectedMessage.setReplyMarkup(buildGrid(callback + data, callback,
                List.of("0-50", "51-100", "101-150", "151-200", "201-250", "251-300")));

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void chooseApproachTest() {
        Integer data = (int) RandomUtils.generateRandomLong(1, 300);
        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + getCallback("weightCallback") + data);

        var actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);
        var actualReq = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Выберите количество подходов:");
        expectedMessage.setReplyMarkup(buildGrid(data.toString(), getCallback("approachCallback"),
                IntStream.rangeClosed(1, 10)
                        .mapToObj(String::valueOf)
                        .toList()));

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedMessage, actualMessage),
                () -> Assertions.assertEquals(data, actualReq.getWeight())
        );
    }

    @Test
    public void createExerciseDoneTest() {
        Integer data = (int) RandomUtils.generateRandomLong(1, 10);
        var req = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId)
                .exerciseName(Exercises.BICEPS_5.getDescription())
                .trainingId(RandomUtils.generateRandomLong(1, 15))
                .approach(data)
                .weight(30)
                .quantity(4);

        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + getCallback("approachCallback") + data);

        ExerciseControllerApi mockApi = mock(ExerciseControllerApi.class);
        SendMessage actualMessage;

        try (MockedStatic<RemoteAppController> mockedStatic = mockStatic(RemoteAppController.class)) {
            mockedStatic.when(RemoteAppController::getExerciseControllerApi).thenReturn(mockApi);
            when(mockApi.createExercise(req)).thenReturn(
                    new ResponseContainerResult()
                            .code(200)
                            .message("")
            );
            actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);
        }

        var actualReq = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Упражнение успешно добавлено");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton next = new InlineKeyboardButton();
        next.setText("Добавить следующее упражнение");
        next.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + req.getTrainingId());
        keyboard.add(List.of(next));

        InlineKeyboardButton done = new InlineKeyboardButton();
        done.setText("Завершить");
        done.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + "done");
        keyboard.add(List.of(done));

        markup.setKeyboard(keyboard);
        expectedMessage.setReplyMarkup(markup);

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedMessage, actualMessage),
                () -> Assertions.assertNull(actualReq)
        );
    }

    @Test
    public void createExerciseDoneNegativeTest() {
        Integer data = (int) RandomUtils.generateRandomLong(1, 10);
        var req = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId)
                .exerciseName(Exercises.BICEPS_5.getDescription())
                .trainingId(RandomUtils.generateRandomLong(1, 15))
                .approach(data)
                .weight(30)
                .quantity(4);

        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + getCallback("approachCallback") + data);

        ExerciseControllerApi mockApi = mock(ExerciseControllerApi.class);
        SendMessage actualMessage;

        try (MockedStatic<RemoteAppController> mockedStatic = mockStatic(RemoteAppController.class)) {
            mockedStatic.when(RemoteAppController::getExerciseControllerApi).thenReturn(mockApi);
            when(mockApi.createExercise(req)).thenReturn(
                    new ResponseContainerResult()
                            .code(-300)
                            .message("Ошибка добавления упражнения")
            );

            actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);
        }

        var actualReq = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Ошибка добавления упражнения: \n" +
                "Ошибка добавления упражнения" + "\n Повторите попытку:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton add = new InlineKeyboardButton();
        add.setText("Добавить упражнение");
        add.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + req.getTrainingId());

        keyboard.add(List.of(add));
        markup.setKeyboard(keyboard);
        expectedMessage.setReplyMarkup(markup);

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedMessage, actualMessage),
                () -> Assertions.assertEquals(new RequestContainerCreateExerciseRequest(), actualReq)
        );
    }

    @ParameterizedTest
    @MethodSource("requestFieldsIsNull")
    public void createExerciseErrorTest(RequestContainerCreateExerciseRequest request) {
        int data = (int) RandomUtils.generateRandomLong(1, 10);
        callbackQuery.setData(CREATE_NEW_EXERCISE.getUrl() + getCallback("approachCallback") + data);
        CashComponent.CREATE_EXERCISE_REQUEST.put(chatId, request);

        var actualMessage = createExerciseComponent.createExercise(callbackQuery, chatId);
        var actualReq = CashComponent.CREATE_EXERCISE_REQUEST.get(chatId);

        var expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Ошибка добавления упражнения:\n Повторите попытку:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton add = new InlineKeyboardButton();
        add.setText("Добавить упражнение");
        keyboard.add(List.of(add));

        markup.setKeyboard(keyboard);
        expectedMessage.setReplyMarkup(markup);

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedMessage, actualMessage),
                () -> Assertions.assertEquals(new RequestContainerCreateExerciseRequest(), actualReq)
        );
    }

    @SneakyThrows
    private InlineKeyboardMarkup buildGrid(String data, String callback, List<String> options) {
        Class<?> createExerciseComponentClass = createExerciseComponent.getClass();
        Method privateMethod = createExerciseComponentClass.getDeclaredMethod(
                "buildGrid", String.class, String.class, List.class
        );
        privateMethod.setAccessible(true);
        return (InlineKeyboardMarkup) privateMethod.invoke(createExerciseComponent, data, callback, options);
    }

    @SneakyThrows
    private String getCallback(String fieldName) {
        Class<?> createExerciseComponentClass = createExerciseComponent.getClass();
        Field field = createExerciseComponentClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(createExerciseComponent);
    }
}
