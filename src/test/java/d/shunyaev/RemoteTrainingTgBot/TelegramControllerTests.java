package d.shunyaev.RemoteTrainingTgBot;

import d.shunyaev.RemoteTrainingTgBot.components.services.CreateExerciseComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.CreateTrainingComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.CreateUserComponent;
import d.shunyaev.RemoteTrainingTgBot.components.services.RegistrationComponent;
import d.shunyaev.RemoteTrainingTgBot.controller.TelegramController;
import d.shunyaev.RemoteTrainingTgBot.utils.RandomUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TelegramControllerTests {
    @Mock
    private RegistrationComponent registrationComponent;
    @Mock
    private CreateUserComponent createUserComponent;
    @Mock
    private CreateTrainingComponent createTrainingComponent;
    @Mock
    private CreateExerciseComponent createExerciseComponent;
    @InjectMocks
    private TelegramController telegramController;
    private SendMessage expectedMessage;
    private long chatId;
    private Update update;
    private User user;

    @BeforeEach
    public void setUp() {
        chatId = RandomUtils.generateRandomChatId();

        update = new Update();

        user = new User();
        user.setId(chatId);
        user.setUserName("user_name");
        user.setFirstName("first_name");
        user.setLastName("last_name");

        expectedMessage = new SendMessage();
        expectedMessage.setText("hello");
    }

    @Test
    public void startTest() {
        Message message = new Message();
        message.setText("/start");
        message.setFrom(user);
        message.setChat(new Chat(chatId, ""));
        update.setMessage(message);

        when(registrationComponent.registration(message)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void startRegistrationTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(CREATE_USER.getUrl());

        update.setCallbackQuery(callbackQuery);

        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void chooseGenderTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(CREATE_USER.getUrl() + "ЖЕНЩИНА");

        update.setCallbackQuery(callbackQuery);

        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void chooseGoalsTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(CREATE_USER.getUrl() + "Похудение");

        update.setCallbackQuery(callbackQuery);

        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void chooseTrainingLevelTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(CREATE_USER.getUrl() + "Средний");

        update.setCallbackQuery(callbackQuery);

        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void addEmailTest() {
        var email = RandomUtils.generateRandomEmail();
        Message message = new Message();
        message.setText(email);
        message.setFrom(user);
        message.setChat(new Chat(chatId, ""));

        update.setMessage(message);

        SendMessage beforeMessage = new SendMessage();
        beforeMessage.setChatId(chatId);
        beforeMessage.setText("Введите ваш email:");

        var callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        callbackQuery.setData(CREATE_USER.getUrl() + EMAIL.getUrl() + email);

        putBeforeMessage(beforeMessage);

        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void addWeightTest() {
        var weight = String.valueOf(RandomUtils.generateRandomLong(40, 90));
        Message message = new Message();
        message.setText(weight);
        message.setFrom(user);
        message.setChat(new Chat(chatId, ""));

        update.setMessage(message);

        SendMessage beforeMessage = new SendMessage();
        beforeMessage.setChatId(chatId);
        beforeMessage.setText("Введите ваш вес в формате: \"55\"");

        var callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        callbackQuery.setData(CREATE_USER.getUrl() + WEIGHT.getUrl() + weight);

        putBeforeMessage(beforeMessage);


        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void addHeightTest() {
        var height = String.valueOf(RandomUtils.generateRandomLong(150, 200));
        Message message = new Message();
        message.setText(height);
        message.setFrom(user);
        message.setChat(new Chat(chatId, ""));

        update.setMessage(message);

        SendMessage beforeMessage = new SendMessage();
        beforeMessage.setChatId(chatId);
        beforeMessage.setText("Введите ваш рост в формате: \"180\"");

        var callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        callbackQuery.setData(CREATE_USER.getUrl() + HEIGHT.getUrl() + height);

        putBeforeMessage(beforeMessage);


        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void addDayOfBirthTest() {
        var pattern = "dd.MM.yyyy";
        var dateOfBirth = RandomUtils.generateRandomDate(
                LocalDate.of(1970, 1, 1),
                LocalDate.now().minusYears(18),
                pattern
        );
        Message message = new Message();
        message.setText(dateOfBirth);
        message.setFrom(user);
        message.setChat(new Chat(chatId, ""));

        update.setMessage(message);

        SendMessage beforeMessage = new SendMessage();
        beforeMessage.setChatId(chatId);
        beforeMessage.setText("Введите вашу дату рождения в формате: \"01.01.2000\"");

        var callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        callbackQuery.setData(CREATE_USER.getUrl() + DATE_OF_BIRTH.getUrl() + dateOfBirth);

        putBeforeMessage(beforeMessage);

        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void addLastNameTest() {
        var lastName = "last_name";
        Message message = new Message();
        message.setText(lastName);
        message.setFrom(user);
        message.setChat(new Chat(chatId, ""));

        update.setMessage(message);

        SendMessage beforeMessage = new SendMessage();
        beforeMessage.setChatId(chatId);
        beforeMessage.setText("Введите вашу Фамилию:");

        var callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        callbackQuery.setData(CREATE_USER.getUrl() + LAST_NAME.getUrl() + lastName);

        putBeforeMessage(beforeMessage);

        when(createUserComponent.createUser(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void createNewTrainingTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData("createNewTraining");
        update.setCallbackQuery(callbackQuery);

        when(createTrainingComponent.createTraining(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void chooseDayOfTrainingTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(CREATE_NEW_TRAINING.getUrl() + "Понедельник");

        update.setCallbackQuery(callbackQuery);

        when(createTrainingComponent.createTraining(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void chooseDateTrainingTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(CREATE_NEW_TRAINING.getUrl() + LocalDate.now());

        update.setCallbackQuery(callbackQuery);

        when(createTrainingComponent.createTraining(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void chooseMuscleGroupTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(CREATE_NEW_TRAINING.getUrl() + "Грудные мышцы");

        update.setCallbackQuery(callbackQuery);

        when(createTrainingComponent.createTraining(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void createTrainingDoneTest() {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(CREATE_NEW_TRAINING.getUrl() + "done");

        update.setCallbackQuery(callbackQuery);

        when(createTrainingComponent.createTraining(callbackQuery, chatId)).thenReturn(expectedMessage);

        var actualMessage = telegramController.createController(update);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @SneakyThrows
    private void putBeforeMessage(SendMessage beforeMessage) {
        Map<Long, SendMessage> mockMap = Mockito.mock(Map.class);

        when(mockMap.get(Mockito.anyLong())).thenReturn(beforeMessage);

        Field field = TelegramController.class.getDeclaredField("beforeMessages");
        field.setAccessible(true);
        field.set(telegramController, mockMap);

    }
}
