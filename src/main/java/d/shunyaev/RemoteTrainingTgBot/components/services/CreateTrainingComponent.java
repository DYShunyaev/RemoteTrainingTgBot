package d.shunyaev.RemoteTrainingTgBot.components.services;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.ValidateComponent;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetTrainingsSteps;
import d.shunyaev.RemoteTrainingTgBot.components.getters_components.GetUserInfoComponent;
import d.shunyaev.RemoteTrainingTgBot.config.request_interceptors.BadRequestException;
import d.shunyaev.RemoteTrainingTgBot.controller.RemoteAppController;
import d.shunyaev.RemoteTrainingTgBot.enums.MuscleGroup;
import d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl;
import d.shunyaev.RemoteTrainingTgBot.utils.ConvertedUtils;
import d.shunyaev.model.RequestContainerCreateTrainingRequest;
import d.shunyaev.model.RequestContainerCreateTrainingRequest.DayOfWeekEnum;
import d.shunyaev.model.RequestContainerGenerateTrainingRequest;
import d.shunyaev.model.RequestContainerGenerateTrainingRequest.DayOfWeekFirstTrainingEnum;
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

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.*;

@Component
public class CreateTrainingComponent {

    private final RegistrationComponent registrationComponent;
    private final ValidateComponent validateComponent;
    private final GetUserInfoComponent getUserInfoComponent;
    private final GetTrainingsSteps getTrainingsComponent;
    private final Map<Long, Set<String>> selectedOptions = new HashMap<>();

    public CreateTrainingComponent(
            RegistrationComponent registrationComponent,
            ValidateComponent validateComponent,
            GetUserInfoComponent getUserInfoComponent,
            GetTrainingsSteps getTrainingsComponent
    ) {
        this.registrationComponent = registrationComponent;
        this.validateComponent = validateComponent;
        this.getUserInfoComponent = getUserInfoComponent;
        this.getTrainingsComponent = getTrainingsComponent;
    }

    public SendMessage createOrGenerate(long chatId) {
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);

        if (!validateComponent.isRegistration(chatId)) {
            responseMessage = registrationComponent.addRegistrationButton(responseMessage, chatId);
            responseMessage.setText("Пользователь еще не зарегистрирован, пройдите регистрацию:");
            return responseMessage;
        }

        responseMessage.setText("Выберете нужный вариант:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton first = new InlineKeyboardButton();
        first.setText("Добавить тренировку самостоятельно");
        first.setCallbackData(CREATE_NEW_TRAINING.getUrl());
        keyboard.add(List.of(first));

        InlineKeyboardButton second = new InlineKeyboardButton();
        second.setText("Сгенерировать тренировку");
        second.setCallbackData(GENERATE_NEW_TRAINING.getUrl());
        keyboard.add(List.of(second));

        markup.setKeyboard(keyboard);
        responseMessage.setReplyMarkup(markup);

        return responseMessage;
    }

    public SendMessage createTraining(CallbackQuery callbackQuery, long chatId) {
        RequestContainerCreateTrainingRequest req = getTrainingRequest(chatId);
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);

        String data = Optional.ofNullable(callbackQuery)
                .map(CallbackQuery::getData)
                .map(d -> d.replaceAll(CREATE_NEW_TRAINING.getUrl(), ""))
                .orElse("");

        if (data.isEmpty()) return chooseDayOfWeek(responseMessage, CREATE_NEW_TRAINING);

        if (Arrays.stream(DayOfWeekEnum.values()).map(DayOfWeekEnum::getValue)
                .collect(Collectors.toSet()).contains(data)) {
            req.setDayOfWeek(DayOfWeekEnum.fromValue(data));
            req.setUserId(getUserInfoComponent.getUserId(callbackQuery.getFrom().getUserName()));
            return chooseDateOfTraining(responseMessage, data, CREATE_NEW_TRAINING);
        }

        if (data.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            req.setDate(data);
            return chooseMuscleGroup(responseMessage);
        }

        if (Arrays.stream(MuscleGroup.values()).map(MuscleGroup::getDescription)
                .collect(Collectors.toSet()).contains(data)) {
            selectedOptions.compute(chatId, (k, v) -> {
                Set<String> s = (v == null) ? new HashSet<>() : v;
                s.add(data);
                return s;
            });
            return chooseMuscleGroup(responseMessage);
        }

        if ("done".equals(data)) {
            req.setMuscleGroup(String.join(" + ", selectedOptions.get(chatId)));
            return callSetNewTraining(responseMessage, req, chatId);
        }

        return responseMessage;
    }

    public SendMessage generateNewTraining(CallbackQuery callbackQuery, long chatId) {
        RequestContainerGenerateTrainingRequest req = getGenerateTrainingRequest(chatId);
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);

        String data = Optional.ofNullable(callbackQuery)
                .map(CallbackQuery::getData)
                .map(d -> d.replaceAll(GENERATE_NEW_TRAINING.getUrl(), ""))
                .orElse("");

        if (data.isEmpty()) {
            return chooseDayOfWeek(responseMessage, GENERATE_NEW_TRAINING);
        } else if (Arrays.stream(DayOfWeekFirstTrainingEnum.values())
                .map(DayOfWeekFirstTrainingEnum::getValue)
                .collect(Collectors.toSet()).contains(data)) {
            req.setDayOfWeekFirstTraining(DayOfWeekFirstTrainingEnum.fromValue(data));
            req.setUserId(getUserInfoComponent.getUserId(callbackQuery.getFrom().getUserName()));
            return chooseDateOfTraining(responseMessage, data, GENERATE_NEW_TRAINING);
        } else if (data.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            req.setDateFirstTraining(data);
            return chooseCountOfTraining(responseMessage, req.getDayOfWeekFirstTraining());
        } else if (data.matches("\\d")) {
            req.setCount(Integer.parseInt(data));
            return callGenerateNewTraining(responseMessage, req, chatId);
        }

        return responseMessage;
    }

    public SendMessage chooseCountOfTraining(SendMessage response, DayOfWeekFirstTrainingEnum day) {
        response.setText("Выберете количество тернировок в неделе");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int rangeEnd = 0;

        switch (day) {
            case u -> rangeEnd = 8;
            case u2 -> rangeEnd = 7;
            case u3 -> rangeEnd = 6;
            case u4 -> rangeEnd = 5;
            case u5 -> rangeEnd = 4;
            case u6 -> rangeEnd = 3;
            case u7 -> rangeEnd = 2;
        }

        for (int i = 1; i < rangeEnd; i++) {
            InlineKeyboardButton b = new InlineKeyboardButton();
            b.setText(String.valueOf(i));
            b.setCallbackData(GENERATE_NEW_TRAINING.getUrl() + i);
            row.add(b);
            if (row.size() == 3) {
                keyboard.add(row);
                row = new ArrayList<>();
            }
        }

        if (!row.isEmpty()) keyboard.add(row);
        markup.setKeyboard(keyboard);

        response.setReplyMarkup(markup);
        return response;
    }

    public SendMessage chooseDateOfTraining(SendMessage response, String dayOfWeek, ServicesUrl url) {
        DayOfWeek day = ConvertedUtils.convertToDayOfWeek(dayOfWeek);
        List<LocalDate> options = getDaysFromTraining(LocalDate.now(), day);

        response.setText("Выберете день тренировки:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (LocalDate d : options) {
            InlineKeyboardButton b = new InlineKeyboardButton();
            b.setText(d.getDayOfMonth() + " " + ConvertedUtils.convertMonthToRussian(d));
            b.setCallbackData(url.getUrl() + d);
            keyboard.add(List.of(b));
        }

        markup.setKeyboard(keyboard);
        response.setReplyMarkup(markup);
        return response;
    }

    private SendMessage chooseDayOfWeek(SendMessage response, ServicesUrl url) {
        response.setText("Выберете день недели тренировки:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (DayOfWeekEnum day : DayOfWeekEnum.values()) {
            InlineKeyboardButton b = new InlineKeyboardButton();
            String value = day.getValue();
            b.setText(value);
            b.setCallbackData(url.getUrl() + value);
            keyboard.add(List.of(b));
        }

        markup.setKeyboard(keyboard);
        response.setReplyMarkup(markup);
        return response;
    }

    public SendMessage chooseMuscleGroup(SendMessage response) {
        response.setText("Выберете цель тренировки:");
        Set<String> before = Optional.ofNullable(selectedOptions.get(Long.parseLong(response.getChatId())))
                .orElse(null);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (MuscleGroup mg : MuscleGroup.values()) {
            String desc = mg.getDescription();
            if (Objects.nonNull(before) && before.contains(desc)) continue;

            InlineKeyboardButton b = new InlineKeyboardButton();
            b.setText(desc);
            b.setCallbackData(CREATE_NEW_TRAINING.getUrl() + desc);
            keyboard.add(List.of(b));
        }

        InlineKeyboardButton confirm = new InlineKeyboardButton();
        confirm.setText("✅ Подтвердить");
        confirm.setCallbackData(CREATE_NEW_TRAINING.getUrl() + "done");
        keyboard.add(List.of(confirm));

        markup.setKeyboard(keyboard);
        response.setReplyMarkup(markup);
        return response;
    }

    private SendMessage callSetNewTraining(SendMessage responseMessage,
                                           RequestContainerCreateTrainingRequest request,
                                           long chatId) {
        if (Objects.nonNull(request.getDate()) &&
                Objects.nonNull(request.getMuscleGroup()) &&
                Objects.nonNull(request.getDayOfWeek()) &&
                Objects.nonNull(request.getUserId())) {

            ResponseContainerResult result;
            try {
                result = RemoteAppController.getTrainingControllerApi().createTraining(request);
            } catch (BadRequestException e) {
                result = e.getResponseBody();
            }

            assert result.getCode() != null;
            if (result.getCode().equals(200)) {
                CashComponent.CREATE_TRAINING_REQUESTS.remove(chatId);
                responseMessage.setText("Тренировка успешно добавлена");

                var trainingId = Objects.requireNonNull(getTrainingsComponent.getTrainingsByChatId(chatId)
                                .getTrainings())
                        .stream()
                        .filter(t -> t.getMuscleGroup().equals(request.getMuscleGroup())
                                && t.getDate().equals(request.getDate())
                                && t.getDayOfWeek().equals(request.getDayOfWeek().getValue()))
                        .findFirst()
                        .get()
                        .getTrainingId();

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                InlineKeyboardButton b = new InlineKeyboardButton();
                b.setText("Добавить первое упражнение ");
                b.setCallbackData(CREATE_NEW_EXERCISE.getUrl() + trainingId);
                keyboard.add(List.of(b));
                markup.setKeyboard(keyboard);
                responseMessage.setReplyMarkup(markup);

            } else if (result.getCode().equals(-8)) {
                assert result.getMessage() != null;
                responseMessage.setText(result.getMessage());

            } else {
                responseMessage.setText("Ошибка добавления тренировки: \n" +
                        result.getMessage() + "\n Повторите попытку:");
                CashComponent.CREATE_TRAINING_REQUESTS.put(chatId, new RequestContainerCreateTrainingRequest());
            }
        } else {
            responseMessage.setText("Ошибка добавления тренировки:\n Повторите попытку:");
            CashComponent.CREATE_TRAINING_REQUESTS.put(chatId, new RequestContainerCreateTrainingRequest());
        }

        return responseMessage;
    }

    private SendMessage callGenerateNewTraining(SendMessage responseMessage,
                                                RequestContainerGenerateTrainingRequest request, long chatId) {
        if (Objects.nonNull(request.getDateFirstTraining()) &&
        Objects.nonNull(request.getCount()) &&
        Objects.nonNull(request.getDayOfWeekFirstTraining()) &&
        Objects.nonNull(request.getUserId())) {
            ResponseContainerResult result;
            try {
                result = RemoteAppController.getTrainingControllerApi().generateTraining(request);
            } catch (BadRequestException e) {
                result = e.getResponseBody();
            }
            if (result.getCode().equals(200)) {
                CashComponent.GENERATE_TRAINING_REQUESTS.remove(chatId);
                responseMessage.setText("Тренировка успешно добавлена");
            } else if (result.getCode().equals(-8)) {
                assert result.getMessage() != null;
                responseMessage.setText(result.getMessage());

            } else {
                responseMessage.setText("Ошибка добавления тренировки: \n" +
                        result.getMessage() + "\n Повторите попытку:");
                CashComponent.CREATE_TRAINING_REQUESTS.put(chatId, new RequestContainerCreateTrainingRequest());
            }
        } else {
            responseMessage.setText("Ошибка добавления тренировки:\n Повторите попытку:");
            CashComponent.GENERATE_TRAINING_REQUESTS.put(chatId, new RequestContainerGenerateTrainingRequest());
        }

        return responseMessage;
    }

    private List<LocalDate> getDaysFromTraining(LocalDate date, DayOfWeek day) {
        YearMonth ym = YearMonth.from(date);
        List<LocalDate> days = getDaysForMonth(ym, date.getDayOfMonth(), day);
        return days.isEmpty() ? getDaysForMonth(ym.plusMonths(1), 1, day) : days;
    }

    private List<LocalDate> getDaysForMonth(YearMonth yearMonth, int startDay, DayOfWeek day) {
        return IntStream.rangeClosed(startDay, yearMonth.lengthOfMonth())
                .mapToObj(d -> LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), d))
                .filter(d -> d.getDayOfWeek().equals(day))
                .sorted()
                .collect(Collectors.toList());
    }

    private RequestContainerCreateTrainingRequest getTrainingRequest(long chatId) {
        return CashComponent.CREATE_TRAINING_REQUESTS.computeIfAbsent(
                chatId, k -> new RequestContainerCreateTrainingRequest());
    }

    private RequestContainerGenerateTrainingRequest getGenerateTrainingRequest(long chatId) {
        return CashComponent.GENERATE_TRAINING_REQUESTS.computeIfAbsent(
                chatId, k -> new RequestContainerGenerateTrainingRequest());
    }

}