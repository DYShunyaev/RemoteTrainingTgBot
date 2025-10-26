package d.shunyaev.RemoteTrainingTgBot;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.CreateUserComponent;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.RemoteTrainingTgBot.utils.RandomUtils;
import d.shunyaev.model.RequestContainerCreateUserRequest;
import d.shunyaev.model.RequestContainerCreateUserRequest.*;
import jdk.jfr.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateUserTests {

    @InjectMocks
    private CreateUserComponent createUserComponent;
    @Mock
    private UsersBotRepository usersBotRepository;
    private CallbackQuery callbackQuery;
    private long chatId;

    @BeforeEach
    public void setUp() {
        chatId = RandomUtils.generateRandomChatId();
        var request = new RequestContainerCreateUserRequest();
        request.setGender(GenderEnum.MAN);
        CashComponent.CREATE_USER_REQUESTS.put(chatId, request);

        var user = new User();
        user.setId(chatId);
        user.setUserName("user_name");
        user.setFirstName("first_name");
        user.setLastName("last_name");

        callbackQuery = new CallbackQuery();
        callbackQuery.setData(CREATE_USER.getUrl());
        callbackQuery.setFrom(user);

        when(usersBotRepository.getRegistrationFlagByChatId(anyLong())).thenReturn(0);
    }

    private static Stream<GenderEnum> genders() {
        return Arrays.stream(GenderEnum.values())
                .filter(val -> !val.equals(GenderEnum.NON));
    }

    private static Stream<GoalsEnum> goals() {
        return Arrays.stream(GoalsEnum.values());
    }

    private static Stream<TrainingLevelEnum> trainingLevels() {
        return Arrays.stream(TrainingLevelEnum.values());
    }

    private static Stream<IsTrainerEnum> isTrainer() {
        return Arrays.stream(IsTrainerEnum.values());
    }

    @Test
    @Description("Получение соощения \"Выберете пол\"")
    public void setGenderTest() {
        CashComponent.CREATE_USER_REQUESTS.put(chatId, new RequestContainerCreateUserRequest());

        var response = createUserComponent.createUser(callbackQuery, chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Выберите ваш пол:");

        expectedResponse.setReplyMarkup(
                preparingMarkupInLine(GenderEnum.class)
        );

        Assertions.assertEquals(expectedResponse, response);
    }

    @ParameterizedTest
    @MethodSource("genders")
    public void setGoalsTest(GenderEnum gender) {
        CashComponent.CREATE_USER_REQUESTS.put(chatId, new RequestContainerCreateUserRequest());
        callbackQuery.setData(CREATE_USER.getUrl() + gender.getValue());

        var response = createUserComponent.createUser(callbackQuery, chatId);

        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);
        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Выберете ваши цели:");


        expectedResponse.setReplyMarkup(
                preparingMarkupInLine(GoalsEnum.class)
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(gender, requestAfter.getGender())
        );
    }

    @ParameterizedTest
    @MethodSource("goals")
    public void setTrainingLevelTest(GoalsEnum goals) {
        callbackQuery.setData(CREATE_USER.getUrl() + goals.getValue());

        var response = createUserComponent.createUser(callbackQuery, chatId);

        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);
        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Выберете ваш уровень подготовки");


        expectedResponse.setReplyMarkup(
                preparingMarkupInLine(TrainingLevelEnum.class)
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(goals, requestAfter.getGoals())
        );
    }

    @ParameterizedTest
    @MethodSource("trainingLevels")
    public void setIsYouTrainerTest(TrainingLevelEnum trainingLevel) {
        callbackQuery.setData(CREATE_USER.getUrl() + trainingLevel.getValue());

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Вы тренер?");


        expectedResponse.setReplyMarkup(
                preparingMarkupInLine(IsTrainerEnum.class)
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(trainingLevel, requestAfter.getTrainingLevel())
        );
    }

    @ParameterizedTest
    @MethodSource("isTrainer")
    public void setEmailTest(IsTrainerEnum isTrainer) {
        callbackQuery.setData(CREATE_USER.getUrl() + isTrainer.getValue());

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Введите ваш email:");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(isTrainer, requestAfter.getIsTrainer())
        );
    }

    @Test
    public void setWeightTest() {
        var email = RandomUtils.generateRandomEmail();
        callbackQuery.setData(CREATE_USER.getUrl() + EMAIL.getUrl() + email);

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Введите ваш вес в формате: \"55\"");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(email, requestAfter.getEmail())
        );
    }

    @Test
    public void incorrectEmailTest() {
        var email = RandomUtils.generateRandomString(15);
        callbackQuery.setData(CREATE_USER.getUrl() + EMAIL.getUrl() + email);

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Введен некорректный email. \nВведите ваш email:");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertNull(requestAfter.getEmail())
        );
    }

    @Test
    public void setHeightTest() {
        var weight = RandomUtils.generateRandomLong(40, 180);
        callbackQuery.setData(CREATE_USER.getUrl() + WEIGHT.getUrl() + weight);

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Введите ваш рост в формате: \"180\"");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(weight, requestAfter.getWeight())
        );
    }

    @Test
    public void incorrectWeightStringTest() {
        var weight = RandomUtils.generateRandomString(5);
        callbackQuery.setData(CREATE_USER.getUrl() + WEIGHT.getUrl() + weight);

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Введено некорректное значение. \nВведите ваш вес в формате: \"55\"");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertNull(requestAfter.getWeight())
        );
    }

    @Test
    public void setDateOfBirthTest() {
        var height = RandomUtils.generateRandomLong(40, 220);
        callbackQuery.setData(CREATE_USER.getUrl() + HEIGHT.getUrl() + height);

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Введите вашу дату рождения в формате: \"01.01.2000\"");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(height, requestAfter.getHeight())
        );
    }

    @Test
    public void setLastNameTest() {
        var pattern = "dd.MM.yyyy";
        var dateOfBirth = RandomUtils.generateRandomDate(
                LocalDate.of(1970,1,1),
                LocalDate.now().minusYears(18),
                pattern
        );

        callbackQuery.setData(CREATE_USER.getUrl() + DATE_OF_BIRTH.getUrl() + dateOfBirth);

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        LocalDate dateOfBirthLocalDate = LocalDate.parse(dateOfBirth,
                DateTimeFormatter.ofPattern(pattern));
        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Введите вашу Фамилию:");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(dateOfBirthLocalDate.toString(), requestAfter.getDateOfBirth())
        );
    }

    @Test
    public void messageDoneTest() {
        var pattern = "dd.MM.yyyy";
        var dateOfBirth = RandomUtils.generateRandomDate(
                LocalDate.of(1970,1,1),
                LocalDate.now().minusYears(18),
                pattern
        );

        CashComponent.CREATE_USER_REQUESTS.get(chatId)
                .setLastName("last_name");

        callbackQuery.setData(CREATE_USER.getUrl() + DATE_OF_BIRTH.getUrl() + dateOfBirth);

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        LocalDate dateOfBirthLocalDate = LocalDate.parse(dateOfBirth,
                DateTimeFormatter.ofPattern(pattern));
        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("done");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertEquals(dateOfBirthLocalDate.toString(), requestAfter.getDateOfBirth())
        );
    }

    @Test
    public void incorrectDataOfBirthTest() {
        var pattern = "MMMM dd, yyyy";
        var dateOfBirth = RandomUtils.generateRandomDate(
                LocalDate.of(1970,1,1),
                LocalDate.now().minusYears(18),
                pattern
        );

        callbackQuery.setData(CREATE_USER.getUrl() + DATE_OF_BIRTH.getUrl() + dateOfBirth);

        var response = createUserComponent.createUser(callbackQuery, chatId);
        var requestAfter = CashComponent.CREATE_USER_REQUESTS.get(chatId);

        var expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Введено некорректное значение. " +
                "\nВведите вашу дату рождения в формате: \"01.01.2000\"");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedResponse, response),
                () -> Assertions.assertNull( requestAfter.getDateOfBirth())
        );
    }

    private <E extends Enum<E>> InlineKeyboardMarkup preparingMarkupInLine(Class<E> enumClass) {
        var markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboards = new ArrayList<>();

        for (E enumValue : enumClass.getEnumConstants()) {
            if (enumValue instanceof GenderEnum && enumValue.equals(GenderEnum.NON)) {
                continue;
            }

            var button = new InlineKeyboardButton();

            button.setText(enumValue instanceof IsTrainerEnum
                    ? ((IsTrainerEnum) enumValue).getValue()
                    .equals(IsTrainerEnum.NUMBER_0.getValue())
                    ? "Нет"
                    : "Да"
                    : enumValue.toString());

            button.setCallbackData(CREATE_USER.getUrl() + enumValue);
            keyboards.add(List.of(button));
        }

        markupInLine.setKeyboard(keyboards);
        return markupInLine;
    }

}
