package d.shunyaev.RemoteTrainingTgBot.components;

import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.BadRequestException;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.enums.MuscleGroup;
import d.shunyaev.RemoteTrainingTgBot.repositories.UsersBotRepository;
import d.shunyaev.RemoteTrainingTgBot.utils.ConvertedUtils;
import d.shunyaev.model.RequestContainerCreateTrainingRequest;
import d.shunyaev.model.RequestContainerCreateTrainingRequest.DayOfWeekEnum;
import d.shunyaev.model.RequestContainerCreateUserRequest;
import d.shunyaev.model.ResponseContainerResult;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.CREATE_NEW_EXERCISE;
import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.CREATE_NEW_TRAINING;

@Component
public class CreateTrainingComponent {

    private final RegistrationComponent registrationComponent;
    private final ValidateComponent validateComponent;
    private final GetUserInfoComponent getUserInfoComponent;
    private final Map<Long, Set<String>> selectedOptions = new HashMap<>();


    public CreateTrainingComponent(
            RegistrationComponent registrationComponent,
            ValidateComponent validateComponent,
            GetUserInfoComponent getUserInfoComponent
    ) {
        this.registrationComponent = registrationComponent;
        this.validateComponent = validateComponent;
        this.getUserInfoComponent = getUserInfoComponent;
    }

    public SendMessage createTraining(CallbackQuery callbackQuery, long chatId) {
        RequestContainerCreateTrainingRequest createTrainingRequest =
                getTrainingRequest(chatId);
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);
        if (!validateComponent.isRegistration(chatId)) {
            responseMessage = registrationComponent.addRegistrationButton(responseMessage, chatId);
            responseMessage.setText("Пользователь еще не зарегистрирован, пройдите регистрацию:");
            return responseMessage;
        }

        String data = Objects.nonNull(callbackQuery)
                ? callbackQuery.getData().replaceAll(CREATE_NEW_TRAINING.getUrl(), "")
                : "";

        if (data.isEmpty()) {
            return chooseDayOfWeek(responseMessage);
        } else if (
                Arrays.stream(DayOfWeekEnum.values()).map(DayOfWeekEnum::getValue)
                        .collect(Collectors.toSet()).contains(data)
        ) {
            createTrainingRequest.setDayOfWeek(DayOfWeekEnum.fromValue(data));
            createTrainingRequest.setUserId(
                    getUserInfoComponent.getUserId(callbackQuery.getFrom().getUserName())
            );
            return chooseDateOfTraining(responseMessage, data);
        } else if (data.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            createTrainingRequest.setDate(data);
            return chooseMuscleGroup(responseMessage);
        } else if (
                Arrays.stream(MuscleGroup.values()).map(MuscleGroup::getDescription)
                        .collect(Collectors.toSet()).contains(data)
        ) {
            Set<String> options = selectedOptions.get(chatId);
            if (Objects.isNull(options)) {
                selectedOptions.put(chatId, new HashSet<>(Set.of(data)));
            } else {
                options.add(data);
            }
            return chooseMuscleGroup(responseMessage);
        } else if (data.equals("done")) {
            createTrainingRequest.setMuscleGroup(
                    String.join(" + ", selectedOptions.get(chatId))
            );
            return callSetNewTraining(responseMessage, createTrainingRequest, chatId);
        }

        return responseMessage;
    }

    private SendMessage chooseDateOfTraining(SendMessage responseMessage, String dayOfWeek) {
        DayOfWeek day = ConvertedUtils.convertToDayOfWeek(dayOfWeek);

        LocalDate date = LocalDate.now();

        List<LocalDate> daysFromTraining = getDaysFromTraining(date, day);

        responseMessage.setText("Выберете день тренировки:");
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (LocalDate localDate : daysFromTraining) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String rusMonth = ConvertedUtils.convertMonthToRussian(localDate);

            button.setText(localDate.getDayOfMonth() + " " + rusMonth);
            button.setCallbackData(CREATE_NEW_TRAINING.getUrl() + localDate);

            keyboard.add(List.of(button));
        }

        markupInLine.setKeyboard(keyboard);
        responseMessage.setReplyMarkup(markupInLine);

        return responseMessage;
    }

    private SendMessage chooseDayOfWeek(SendMessage responseMessage) {
        responseMessage.setText("Выберете день недели тренировки:");
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (DayOfWeekEnum dayOfWeek : DayOfWeekEnum.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String value = dayOfWeek.getValue();

            button.setText(value);
            button.setCallbackData(CREATE_NEW_TRAINING.getUrl() + value);

            keyboard.add(List.of(button));
        }

        markupInLine.setKeyboard(keyboard);
        responseMessage.setReplyMarkup(markupInLine);

        return responseMessage;
    }

    private SendMessage chooseMuscleGroup(SendMessage responseMessage) {
        responseMessage.setText("Выберете цель тренировки:");
        Set<String> groupBefore = selectedOptions.get(Long.parseLong(responseMessage.getChatId()));

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (MuscleGroup muscleGroup : MuscleGroup.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String value = muscleGroup.getDescription();

            if (Objects.nonNull(groupBefore) && groupBefore.contains(value)) {
                continue;
            }

            button.setText(value);
            button.setCallbackData(CREATE_NEW_TRAINING.getUrl() + value);

            keyboard.add(List.of(button));
        }

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("✅ Подтвердить");
        button.setCallbackData(CREATE_NEW_TRAINING.getUrl() + "done");
        keyboard.add(List.of(button));

        markupInLine.setKeyboard(keyboard);
        responseMessage.setReplyMarkup(markupInLine);

        return responseMessage;
    }

    private SendMessage callSetNewTraining(
            SendMessage responseMessage,
            RequestContainerCreateTrainingRequest request,
            long chatId
    ) {
        if (Objects.nonNull(request.getDate())
                && Objects.nonNull(request.getMuscleGroup())
                && Objects.nonNull(request.getDayOfWeek())
                && Objects.nonNull(request.getUserId())) {
            ResponseContainerResult responseContainerResult;
            try {
                responseContainerResult = RemoteAppController.getTrainingControllerApi().createTraining(request);
            } catch (BadRequestException e) {
                responseContainerResult = e.getResponseBody();
            }
            assert responseContainerResult.getCode() != null;
            if (responseContainerResult.getCode().equals(200)) {
                CashComponent.CREATE_TRAINING_REQUESTS.remove(chatId);
                responseMessage.setText("Тренировка успешно добавлена");

                InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("Добавить первое упражнение ");
                button.setCallbackData(CREATE_NEW_EXERCISE.getUrl());
                keyboard.add(List.of(button));

                markupInLine.setKeyboard(keyboard);
                responseMessage.setReplyMarkup(markupInLine);

            } else if (responseContainerResult.getCode().equals(-8)) {
                assert responseContainerResult.getMessage() != null;
                responseMessage.setText(responseContainerResult.getMessage());

            } else {
                responseMessage.setText("Ошибка добавления тренировки: \n" + responseContainerResult.getMessage()
                        + "\n Повторите попытку:");
                CashComponent.CREATE_TRAINING_REQUESTS.put(chatId, new RequestContainerCreateTrainingRequest());
            }
        } else {
            responseMessage.setText("Ошибка добавления тренировки:"
                    + "\n Повторите попытку:");
            CashComponent.CREATE_TRAINING_REQUESTS.put(chatId, new RequestContainerCreateTrainingRequest());
        }

        return responseMessage;
    }

    private List<LocalDate> getDaysFromTraining(LocalDate date, DayOfWeek day) {
        YearMonth yearMonth = YearMonth.from(date);
        List<LocalDate> daysFromTraining = getDaysForMonth(yearMonth, date.getDayOfMonth(), day);

        return daysFromTraining.isEmpty()
                ? getDaysForMonth(yearMonth.plusMonths(1), 1, day)
                : daysFromTraining;
    }

    private List<LocalDate> getDaysForMonth(YearMonth yearMonth, int startDay, DayOfWeek day) {
        return IntStream.rangeClosed(startDay, yearMonth.lengthOfMonth())
                .mapToObj(dayOfMonth -> LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), dayOfMonth))
                .filter(d -> d.getDayOfWeek().equals(day))
                .sorted()
                .collect(Collectors.toList());
    }

    private RequestContainerCreateTrainingRequest getTrainingRequest(long chatId) {
        return CashComponent.CREATE_TRAINING_REQUESTS.computeIfAbsent(chatId,
                k -> new RequestContainerCreateTrainingRequest());
    }
}
