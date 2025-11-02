package d.shunyaev.RemoteTrainingTgBot.utils;

import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalDate;

@UtilityClass
public class ConvertedUtils {

    public DayOfWeek convertToDayOfWeek(String dayOfWeek) {
        DayOfWeek day;

        switch (dayOfWeek) {
            case "Понедельник" -> day = DayOfWeek.MONDAY;
            case "Вторник" -> day = DayOfWeek.TUESDAY;
            case "Среда" -> day = DayOfWeek.WEDNESDAY;
            case "Четверг" -> day = DayOfWeek.THURSDAY;
            case "Пятница" -> day = DayOfWeek.FRIDAY;
            case "Суббота" -> day = DayOfWeek.SATURDAY;
            case "Воскресенье" -> day = DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Некорректный день недели: " + dayOfWeek);
        }
        return day;
    }

    public String convertMonthToRussian(LocalDate date) {
        String month = date.getMonth().name();
        return switch (month.toUpperCase()) {
            case "JANUARY" -> "Января";
            case "FEBRUARY" -> "Февраля";
            case "MARCH" -> "Марта";
            case "APRIL" -> "Апреля";
            case "MAY" -> "Мая";
            case "JUNE" -> "Июня";
            case "JULY" -> "Июля";
            case "AUGUST" -> "Августа";
            case "SEPTEMBER" -> "Сентября";
            case "OCTOBER" -> "Октября";
            case "NOVEMBER" -> "Ноября";
            case "DECEMBER" -> "Декабря";
            default -> throw new IllegalArgumentException("Некорректное название месяца: " + month);
        };
    }
}
