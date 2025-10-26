package d.shunyaev.RemoteTrainingTgBot.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class RandomUtils {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";

    public Long generateRandomChatId() {
        Random random = new Random();
        return 1000000000L + (long)(random.nextDouble() * 9000000000L);
    }

    public String generateRandomEmail() {
        String[] DOMAINS = { "gmail.com", "yahoo.com", "hotmail.com", "outlook.com",
                "example.com", "mail.ru", "yandex.ru"};
        String localPart = generateRandomString(8);
        String domain = DOMAINS[new Random().nextInt(DOMAINS.length)];
        return localPart + "@" + domain;
    }

    public String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }

    public long generateRandomLong(long min, long max) {
        Random random = new Random();
        return min + (long)(random.nextDouble() * (max - min));
    }

    public static String generateRandomDate(LocalDate startDate, LocalDate endDate, String format) {
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);

        LocalDate randomDate = LocalDate.ofEpochDay(randomEpochDay);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        return randomDate.format(formatter);
    }
}
