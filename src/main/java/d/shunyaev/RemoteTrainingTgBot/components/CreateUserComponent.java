package d.shunyaev.RemoteTrainingTgBot.components;

import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.model.RequestContainerCreateUserRequest;
import d.shunyaev.model.ResponseContainerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
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

    public CreateUserComponent(UsersBotRepository usersBotRepository) {
        this.usersBotRepository = usersBotRepository;
    }

    public SendMessage createUser(CallbackQuery callbackQuery) {
        Message message = (Message) callbackQuery.getMessage();
        RequestContainerCreateUserRequest createUserRequest =
                CashComponent.CREATE_USER_REQUESTS.get(message.getChatId());
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(message.getChatId());

        if (usersBotRepository.getRegistrationFlagByChatId(message.getChatId()) == 1) {
            responseMessage.setText("Пользователь уже зарегистрирован");
            return responseMessage;
        }

        String data = callbackQuery.getData().replaceAll(CREATE_USER.getUrl(), "");

        if (Objects.isNull(createUserRequest.getGender())
                || createUserRequest.getGender().equals(RequestContainerCreateUserRequest.GenderEnum.NON)) {
            if (RequestContainerCreateUserRequest.GenderEnum.MAN.getValue().equals(data)
                    || RequestContainerCreateUserRequest.GenderEnum.WOMAN.getValue().equals(data)) {
                createUserRequest.setGender(
                        Arrays.stream(RequestContainerCreateUserRequest.GenderEnum.values())
                                .filter(value -> value.getValue().equals(data))
                                .findFirst()
                                .get()
                );
                return preparingResponseMessage(responseMessage, "Выберете ваши цели:",
                        RequestContainerCreateUserRequest.GoalsEnum.class);

            } else {
                User user = callbackQuery.getFrom();
                createUserRequest
                        .userName(user.getUserName())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName());

                return preparingResponseMessage(responseMessage, "Выберите ваш пол:",
                        RequestContainerCreateUserRequest.GenderEnum.class);
            }
        } else if (Arrays.stream(RequestContainerCreateUserRequest.GoalsEnum.values())
                .map(RequestContainerCreateUserRequest.GoalsEnum::getValue)
                .collect(Collectors.toSet()).contains(data)) {
            createUserRequest.setGoals(
                    Arrays.stream(RequestContainerCreateUserRequest.GoalsEnum.values())
                            .filter(value -> value.getValue().equals(data))
                            .findFirst()
                            .get()
            );
            return preparingResponseMessage(responseMessage, "Выберете ваш уровень подготовки",
                    RequestContainerCreateUserRequest.TrainingLevelEnum.class);
        } else if (Arrays.stream(RequestContainerCreateUserRequest.TrainingLevelEnum.values())
                .map(RequestContainerCreateUserRequest.TrainingLevelEnum::getValue)
                .collect(Collectors.toSet()).contains(data)) {
            createUserRequest.setTrainingLevel(
                    Arrays.stream(RequestContainerCreateUserRequest.TrainingLevelEnum.values())
                            .filter(value -> value.getValue().equals(data))
                            .findFirst()
                            .get()
            );
            return preparingResponseMessage(responseMessage, "Вы тренер?",
                    RequestContainerCreateUserRequest.IsTrainerEnum.class);
        } else if (data.contains(EMAIL.getUrl())) {
            String email = data.replaceAll(EMAIL.getUrl(), "");
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$")) {
                responseMessage.setText("Введен некорректный email. \nВведите ваш email:");
            } else {
                createUserRequest.setEmail(email);
                responseMessage.setText("Введите ваш вес в формате: \"55\"");
            }
            return responseMessage;
        } else if (data.contains(WEIGHT.getUrl())) {
            try {
                long weight = Long.parseLong(data.replaceAll(WEIGHT.getUrl(), ""));
                createUserRequest.setWeight(weight);
                responseMessage.setText("Введите ваш рост в формате: \"180\"");
            } catch (NumberFormatException e) {
                responseMessage.setText("Введено некорректное значение. \nВведите ваш вес в формате: \"55\"");
            }
            return responseMessage;
        } else if (data.contains(HEIGHT.getUrl())) {
            try {
                long height = Long.parseLong(data.replaceAll(HEIGHT.getUrl(), ""));
                createUserRequest.setHeight(height);
                responseMessage.setText("Введите вашу дату рождения в формате: \"01.01.2000\"");
                return responseMessage;
            } catch (NumberFormatException e) {
                responseMessage.setText("Введено некорректное значение. \nВведите ваш рост в формате: \"180\"");
                return responseMessage;
            }
        } else if (data.contains(DATE_OF_BIRTH.getUrl())) {
            try {
                String dateOfBirthStr = data.replaceAll(DATE_OF_BIRTH.getUrl(), "");
                LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr,
                        DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                createUserRequest.setDateOfBirth(dateOfBirth.toString());
                if (Objects.isNull(createUserRequest.getLastName())) {
                    responseMessage.setText("Введите вашу Фамилию:");
                    return responseMessage;
                }
            } catch (DateTimeParseException e) {
                responseMessage.setText("Введено некорректное значение. " +
                        "\nВведите вашу дату рождения в формате: \"01.01.2000\"");
                return responseMessage;
            }
        } else if (data.contains(LAST_NAME.getUrl())) {
            String lastName = data.replaceAll(LAST_NAME.getUrl(), "");
            createUserRequest.setLastName(lastName);
        }else if (Arrays.stream(RequestContainerCreateUserRequest.IsTrainerEnum.values())
                .map(RequestContainerCreateUserRequest.IsTrainerEnum::getValue)
                .collect(Collectors.toSet()).contains(Integer.parseInt(data))) {
            createUserRequest.setIsTrainer(
                    Arrays.stream(RequestContainerCreateUserRequest.IsTrainerEnum.values())
                            .filter(value -> value.getValue().toString().equals(data))
                            .findFirst()
                            .get()
            );
            responseMessage.setText("Введите ваш email:");
            return responseMessage;
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
            ResponseContainerResult responseContainerResult =
                    RemoteAppController.getUserControllerApi().createUser(createUserRequest);
            if (responseContainerResult.getCode().equals(200)) {
                usersBotRepository.setFlagRegistration(message.getChatId());
                responseMessage.setText("Регистрация завершена");
            }
        }
        return responseMessage;
    }

    private <E extends Enum<E>> SendMessage preparingResponseMessage(SendMessage responseMessage, String text,
                                                                     Class<E> enumClass) {
        responseMessage.setText(text);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        E[] enumValues = enumClass.getEnumConstants();

        for (E enumValue : enumValues) {
            if (enumValue instanceof RequestContainerCreateUserRequest.GenderEnum &&
                    enumValue.equals(RequestContainerCreateUserRequest.GenderEnum.NON)) {
                continue;
            }

            InlineKeyboardButton button = new InlineKeyboardButton();

            button.setText(enumValue instanceof RequestContainerCreateUserRequest.IsTrainerEnum
                    ? ((RequestContainerCreateUserRequest.IsTrainerEnum) enumValue).getValue()
                    .equals(RequestContainerCreateUserRequest.IsTrainerEnum.NUMBER_0.getValue())
                    ? "Нет"
                    : "Да"
                    : enumValue.toString());

            button.setCallbackData(CREATE_USER.getUrl() + enumValue);

            List<InlineKeyboardButton> buttonList = new ArrayList<>();
            buttonList.add(button);

            keyboard.add(buttonList);
        }

        markupInLine.setKeyboard(keyboard);
        responseMessage.setReplyMarkup(markupInLine);

        return responseMessage;
    }

}