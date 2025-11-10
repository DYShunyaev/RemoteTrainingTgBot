package d.shunyaev.RemoteTrainingTgBot.components;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class BuildGridComponent {

    public final String quantityCallback = "quantity/";
    public final String approachCallback = "approach/";
    public final String weightCallback = "weight/";

    public InlineKeyboardMarkup buildGrid(String data, String callback, List<String> options, String url) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (data.contains(callback)) {
            var range = Arrays.stream(data.replaceAll(callback, "")
                            .split("-"))
                    .map(Integer::valueOf)
                    .sorted()
                    .toList();

            for (int i = range.get(0); i <= range.get(1); i++) {
                row.add(createButton(url + callback + i, String.valueOf(i)));
                if (row.size() == 3) {
                    keyboard.add(row);
                    row = new ArrayList<>();
                }
            }
        } else {
            for (String opt : options) {
                row.add(createButton(url + callback + opt, opt));
                if (row.size() == 3) {
                    keyboard.add(row);
                    row = new ArrayList<>();
                }
            }
        }

        if (!row.isEmpty()) keyboard.add(row);
        markup.setKeyboard(keyboard);
        return markup;
    }

    private InlineKeyboardButton createButton(String data, String text) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(data);
        return b;
    }
}
