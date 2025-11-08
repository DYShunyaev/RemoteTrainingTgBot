package d.shunyaev.RemoteTrainingTgBot;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.ValidateComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetUserInfoComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.CreateTrainingComponent;
import d.shunyaev.RemoteTrainingTgBot.enums.MuscleGroup;
import d.shunyaev.RemoteTrainingTgBot.utils.RandomUtils;
import d.shunyaev.model.RequestContainerCreateTrainingRequest;
import d.shunyaev.model.RequestContainerCreateTrainingRequest.DayOfWeekEnum;
import d.shunyaev.model.RequestContainerCreateUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.CREATE_NEW_TRAINING;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateTrainingTests {

    @InjectMocks
    private CreateTrainingComponent createTrainingComponent;
    @Mock
    private ValidateComponent validateComponent;
    @Mock
    private GetUserInfoComponent getUserInfoComponent;
    private CallbackQuery callbackQuery;
    private long chatId;

    @BeforeEach
    public void setUp() {
        chatId = RandomUtils.generateRandomChatId();
        var request = new RequestContainerCreateUserRequest();
        request.setGender(RequestContainerCreateUserRequest.GenderEnum.u);
        CashComponent.CREATE_USER_REQUESTS.put(chatId, request);

        var user = new User();
        user.setId(chatId);
        user.setUserName("user_name");
        user.setFirstName("first_name");
        user.setLastName("last_name");

        callbackQuery = new CallbackQuery();
        callbackQuery.setData(CREATE_NEW_TRAINING.getUrl());
        callbackQuery.setFrom(user);
    }

    private static Stream<String> daysOfWeek() {
        return Arrays.stream(DayOfWeekEnum.values())
                .map(DayOfWeekEnum::getValue);
    }

    private static Stream<Long> countScip() {
        return IntStream.range(1, MuscleGroup.values().length)
                .mapToObj(Long::valueOf);
    }

    @Test
    public void chooseDayOfWeekTest() {
        SendMessage expectedMessage = new SendMessage();

        expectedMessage.setChatId(chatId);
        expectedMessage.setText("Выберете день недели тренировки:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (DayOfWeekEnum day : DayOfWeekEnum.values()) {
            InlineKeyboardButton b = new InlineKeyboardButton();
            String value = day.getValue();
            b.setText(value);
            b.setCallbackData(CREATE_NEW_TRAINING.getUrl() + value);
            keyboard.add(List.of(b));
        }

        markup.setKeyboard(keyboard);
        expectedMessage.setReplyMarkup(markup);

        callbackQuery.setData(null);

        var actualMessage = createTrainingComponent.createTraining(callbackQuery, chatId);
        var cashReq = CashComponent.CREATE_TRAINING_REQUESTS.get(chatId);

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedMessage, actualMessage),
                () -> Assertions.assertNotNull(cashReq)
        );
    }

    @ParameterizedTest
    @MethodSource("daysOfWeek")
    public void chooseDateOfTraining(String dayOfWeek) {
        when(getUserInfoComponent.getUserId(anyString())).thenReturn(1L);

        CashComponent.CREATE_TRAINING_REQUESTS.put(chatId, new RequestContainerCreateTrainingRequest());
        callbackQuery.setData(CREATE_NEW_TRAINING.getUrl() + dayOfWeek);

        var actualMessage = createTrainingComponent.createTraining(callbackQuery, chatId);
        var cashReq = CashComponent.CREATE_TRAINING_REQUESTS.get(chatId);

        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);

        expectedMessage = createTrainingComponent.chooseDateOfTraining(expectedMessage, dayOfWeek, CREATE_NEW_TRAINING);

        SendMessage finalExpectedMessage = expectedMessage;
        var expectedDayOfWeek = Arrays.stream(DayOfWeekEnum.values())
                .filter(i -> i.getValue().equals(dayOfWeek))
                .findFirst()
                .get();

        Assertions.assertAll(
                () -> Assertions.assertEquals(finalExpectedMessage, actualMessage),
                () -> Assertions.assertEquals(expectedDayOfWeek, cashReq.getDayOfWeek())
        );
    }

    @Test
    public void chooseMuscleGroupTest() {
        var pattern = "yyyy-MM-dd";
        var trainingDate = RandomUtils.generateRandomDate(
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                pattern
        );

        CashComponent.CREATE_TRAINING_REQUESTS.put(chatId, new RequestContainerCreateTrainingRequest());
        callbackQuery.setData(CREATE_NEW_TRAINING.getUrl() + trainingDate);

        var actualMessage = createTrainingComponent.createTraining(callbackQuery, chatId);
        var cashReq = CashComponent.CREATE_TRAINING_REQUESTS.get(chatId);

        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage = createTrainingComponent.chooseMuscleGroup(expectedMessage);

        SendMessage finalExpectedMessage = expectedMessage;
        Assertions.assertAll(
                () -> Assertions.assertEquals(finalExpectedMessage, actualMessage),
                () -> Assertions.assertEquals(trainingDate, cashReq.getDate())
        );
    }

    @ParameterizedTest
    @MethodSource("countScip")
    public void chooseMuscleGroup2Test(Long countScip) throws NoSuchFieldException, IllegalAccessException {
        var muscleGroup = MuscleGroup.CHEST.getDescription();
        Map<Long, Set<String>> mockMap = Mockito.mock(Map.class);
        Set<String> mockSet = new HashSet<>();

        var muscleGroupValues = MuscleGroup.values();
        var mgvAfterScip = Arrays.stream(muscleGroupValues).skip(countScip).collect(Collectors.toSet());
        for (MuscleGroup muscleGroups : mgvAfterScip) {
            mockSet.add(muscleGroups.getDescription());
        }

        when(mockMap.get(Mockito.anyLong())).thenReturn(mockSet);

        Field field = CreateTrainingComponent.class.getDeclaredField("selectedOptions");
        field.setAccessible(true);
        field.set(createTrainingComponent, mockMap);

        CashComponent.CREATE_TRAINING_REQUESTS.put(chatId, new RequestContainerCreateTrainingRequest());
        callbackQuery.setData(CREATE_NEW_TRAINING.getUrl() + muscleGroup);

        var actualMessage = createTrainingComponent.createTraining(callbackQuery, chatId);

        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId);
        expectedMessage = createTrainingComponent.chooseMuscleGroup(expectedMessage);

        SendMessage finalExpectedMessage = expectedMessage;
        Assertions.assertEquals(finalExpectedMessage, actualMessage);
    }
}
