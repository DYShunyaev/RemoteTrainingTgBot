package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetUserInfoComponent;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.BadRequestException;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.model.RequestContainerCreateUserRequest;
import d.shunyaev.model.RequestContainerCreateUserRequest.GenderEnum;
import d.shunyaev.model.RequestContainerCreateUserRequest.GoalsEnum;
import d.shunyaev.model.RequestContainerCreateUserRequest.TrainingLevelEnum;
import d.shunyaev.model.RequestContainerCreateUserRequest.IsTrainerEnum;
import d.shunyaev.model.ResponseContainerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.*;

@Component
@Slf4j
public class CreateUserComponent {

    private final UsersBotRepository usersBotRepository;
    private final RegistrationComponent registrationComponent;
    private final GetUserInfoComponent getUserInfoComponent;
    private final Class<GenderEnum> GENDER = GenderEnum.class;
    private final Class<GoalsEnum> GOALS = GoalsEnum.class;
    private final Class<TrainingLevelEnum> TRAINING_LEVEL = TrainingLevelEnum.class;
    private final Class<IsTrainerEnum> IS_TRAINER = IsTrainerEnum.class;

    public CreateUserComponent(
            UsersBotRepository usersBotRepository,
            RegistrationComponent registrationComponent,
            GetUserInfoComponent getUserInfoComponent
    ) {
        this.usersBotRepository = usersBotRepository;
        this.registrationComponent = registrationComponent;
        this.getUserInfoComponent = getUserInfoComponent;
    }

    public SendMessage createUser(CallbackQuery callbackQuery, long chatId) {
        RequestContainerCreateUserRequest createUserRequest =
                CashComponent.CREATE_USER_REQUESTS.get(chatId);
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);

        if (Objects.nonNull(callbackQuery) && Objects.isNull(callbackQuery.getData())) {
            return responseMessage;
        }
        if (usersBotRepository.getRegistrationFlagByChatId(chatId) == 1
                && callbackQuery.getData().contains(CREATE_USER.getUrl())) {
            responseMessage.setText("Пользователь уже зарегистрирован");
            return responseMessage;
        } else if (usersBotRepository.getRegistrationFlagByChatId(chatId) == 1
                && !callbackQuery.getData().contains(CREATE_USER.getUrl())) {
            return responseMessage;
        }

        String data = callbackQuery.getData().replaceAll(CREATE_USER.getUrl(), "");

        if (Objects.isNull(createUserRequest.getGender())
                || createUserRequest.getGender().equals(GenderEnum.NON)) {
            if (isContainsToData(GENDER, data)) {
                createUserRequest.setGender((GenderEnum) getValue(GENDER, data));
                return preparingResponseMessage(responseMessage, "Выберете ваши цели:", GOALS);

            } else {
                setUserInfo(callbackQuery, createUserRequest);
                return preparingResponseMessage(responseMessage, "Выберите ваш пол:", GENDER);
            }

        } else if (isContainsToData(GOALS, data)) {
            createUserRequest.setGoals((GoalsEnum) getValue(GOALS, data));
            return preparingResponseMessage(responseMessage, "Выберете ваш уровень подготовки", TRAINING_LEVEL);

        } else if (isContainsToData(TRAINING_LEVEL, data)) {
            createUserRequest.setTrainingLevel((TrainingLevelEnum) getValue(TRAINING_LEVEL, data));
            return preparingResponseMessage(responseMessage, "Вы тренер?", IS_TRAINER);

        } else if (isContainsToData(IS_TRAINER, data)) {
            createUserRequest.setIsTrainer((IsTrainerEnum) getValue(IS_TRAINER, data));
            responseMessage.setText("Введите ваш email:");
            return responseMessage;

        } else if (data.contains(EMAIL.getUrl())) {
            return setEmailToRequest(createUserRequest, data, responseMessage);

        } else if (data.contains(WEIGHT.getUrl())) {
            return setLongValueToRequest(
                    createUserRequest,
                    data,
                    responseMessage,
                    "Введите ваш рост в формате: \"180\"",
                    "Введено некорректное значение. \nВведите ваш вес в формате: \"55\"",
                    WEIGHT
            );

        } else if (data.contains(HEIGHT.getUrl())) {
            return setLongValueToRequest(
                    createUserRequest,
                    data,
                    responseMessage,
                    "Введите вашу дату рождения в формате: \"01.01.2000\"",
                    "Введено некорректное значение. \nВведите ваш рост в формате: \"180\"",
                    HEIGHT
            );

        } else if (data.contains(DATE_OF_BIRTH.getUrl())) {
            setDateOfBirthToRequest(createUserRequest, data, responseMessage);
            if (!responseMessage.getText().equals("done")) {
                return responseMessage;
            }

        } else if (data.contains(LAST_NAME.getUrl())) {
            String lastName = data.replaceAll(LAST_NAME.getUrl(), "");
            createUserRequest.setLastName(lastName);
        }
        if (
                Objects.nonNull(createUserRequest.getGender())
                        && Objects.nonNull(createUserRequest.getEmail())
                        && Objects.nonNull(createUserRequest.getHeight())
                        && Objects.nonNull(createUserRequest.getWeight())
                        && Objects.nonNull(createUserRequest.getGoals())
                        && Objects.nonNull(createUserRequest.getUserName())
                        && Objects.nonNull(createUserRequest.getFirstName())
                        && Objects.nonNull(createUserRequest.getLastName())
                        && Objects.nonNull(createUserRequest.getIsTrainer())
                        && Objects.nonNull(createUserRequest.getDateOfBirth())
                        && Objects.nonNull(createUserRequest.getTrainingLevel())
        ) {
            ResponseContainerResult responseContainerResult;
            try {
                responseContainerResult = RemoteAppController.getUserControllerApi().createUser(createUserRequest);
            } catch (BadRequestException e) {
                responseContainerResult = e.getResponseBody();
            }

            assert responseContainerResult.getCode() != null;
            if (responseContainerResult.getCode().equals(200)) {
                usersBotRepository.setFlagRegistration(chatId);

                String userName = createUserRequest.getUserName();
                long userId = getUserInfoComponent.getUserId(userName);
                usersBotRepository.setUserId(userId, userName);

                CashComponent.CREATE_USER_REQUESTS.remove(chatId);
                responseMessage.setText("Регистрация завершена");

            } else if (responseContainerResult.getCode().equals(-8)) {
                assert responseContainerResult.getMessage() != null;
                responseMessage.setText(responseContainerResult.getMessage());

            } else {
                responseMessage = registrationComponent.addRegistrationButton(responseMessage, chatId);
                responseMessage.setText("Ошибка регистрации: \n" + responseContainerResult.getMessage()
                        + "\n Повторите попытку:");
                CashComponent.CREATE_USER_REQUESTS.put(chatId, new RequestContainerCreateUserRequest());
            }
        }
        return responseMessage;
    }

    private <E extends Enum<E>> boolean isContainsToData(Class<E> enumClass, String data) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.toSet()).contains(data);
    }

    private <E extends Enum<E>> Object getValue(Class<E> enumClass, String data) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(value -> value.toString().equals(data))
                .findFirst()
                .get();
    }

    private <E extends Enum<E>> SendMessage preparingResponseMessage(SendMessage responseMessage, String text,
                                                                     Class<E> enumClass) {
        responseMessage.setText(text);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        E[] enumValues = enumClass.getEnumConstants();

        for (E enumValue : enumValues) {
            if (enumValue instanceof GenderEnum && enumValue.equals(GenderEnum.NON)) {
                continue;
            }

            InlineKeyboardButton button = new InlineKeyboardButton();

            button.setText(enumValue instanceof IsTrainerEnum
                    ? ((IsTrainerEnum) enumValue).getValue()
                    .equals(IsTrainerEnum.NUMBER_0.getValue())
                    ? "Нет"
                    : "Да"
                    : enumValue.toString());

            button.setCallbackData(CREATE_USER.getUrl() + enumValue);

            keyboard.add(List.of(button));
        }

        markupInLine.setKeyboard(keyboard);
        responseMessage.setReplyMarkup(markupInLine);

        return responseMessage;
    }

    private SendMessage setEmailToRequest(RequestContainerCreateUserRequest createUserRequest,
                                          String data, SendMessage responseMessage) {
        String email = data.replaceAll(EMAIL.getUrl(), "");
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$")) {
            responseMessage.setText("Введен некорректный email. \nВведите ваш email:");
        } else {
            createUserRequest.setEmail(email);
            responseMessage.setText("Введите ваш вес в формате: \"55\"");
        }
        return responseMessage;
    }

    private SendMessage setLongValueToRequest(RequestContainerCreateUserRequest createUserRequest,
                                              String data, SendMessage responseMessage, String firstInfo,
                                              String secondInfo, ServicesUrl servicesUrl) {
        try {
            long value = Long.parseLong(data.replaceAll(servicesUrl.getUrl(), ""));
            if (value <= 0) {
                responseMessage.setText(("%s не может быть отрицательным или равен 0. \n" +
                        "Повторите попытку ввода:")
                        .formatted(servicesUrl.equals(HEIGHT)
                                ? "Рост"
                                : "Вес"));
                return responseMessage;
            }
            if (servicesUrl.equals(WEIGHT)) {
                createUserRequest.setWeight(value);
            } else if (servicesUrl.equals(HEIGHT)) {
                createUserRequest.setHeight(value);
            }
            responseMessage.setText(firstInfo);
        } catch (NumberFormatException e) {
            responseMessage.setText(secondInfo);
        }
        return responseMessage;
    }

    private void setDateOfBirthToRequest(RequestContainerCreateUserRequest createUserRequest,
                                         String data, SendMessage responseMessage) {
        try {
            String dateOfBirthStr = data.replaceAll(DATE_OF_BIRTH.getUrl(), "");
            LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr,
                    DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            createUserRequest.setDateOfBirth(dateOfBirth.toString());
            responseMessage.setText("done");
            if (Objects.isNull(createUserRequest.getLastName())) {
                responseMessage.setText("Введите вашу Фамилию:");
            }
        } catch (DateTimeParseException e) {
            responseMessage.setText("Введено некорректное значение. " +
                    "\nВведите вашу дату рождения в формате: \"01.01.2000\"");
        }
    }

    private void setUserInfo(CallbackQuery callbackQuery, RequestContainerCreateUserRequest createUserRequest) {
        User user = callbackQuery.getFrom();
        createUserRequest
                .userName(user.getUserName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName());
    }

}