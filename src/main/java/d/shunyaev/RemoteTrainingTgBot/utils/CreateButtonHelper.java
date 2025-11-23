package d.shunyaev.RemoteTrainingTgBot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CreateButtonHelper {

    public List<InlineKeyboardButton> createButtonList(String description, String callbackData) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(description);
        b.setCallbackData(callbackData);
        return List.of(b);
    }

    public InlineKeyboardButton createButton(String data, String text) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(data);
        return b;
    }

    public InlineKeyboardMarkup addMarkupButton(String description, String callbackData) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(
               createButtonList(
                       description,
                       callbackData
                )
        );

        markup.setKeyboard(keyboard);
        return markup;
    }
}
