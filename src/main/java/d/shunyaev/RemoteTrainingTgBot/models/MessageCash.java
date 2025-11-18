package d.shunyaev.RemoteTrainingTgBot.models;

import lombok.Data;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class MessageCash {

    private long chatId;
    private EditMessageText editMessageText;
    private LocalDateTime localDateTime;

    public void setEditMessageText(EditMessageText editMessageText) {
        this.editMessageText = editMessageText;
    }

    public void setEditMessageText(SendMessage sendMessage) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(sendMessage.getText());
        editMessageText.setReplyMarkup((InlineKeyboardMarkup) sendMessage.getReplyMarkup());
        editMessageText.setChatId(sendMessage.getChatId());
        this.editMessageText = editMessageText;
    }

    public void setEditMessageText(Message message) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(message.getText());
        editMessageText.setReplyMarkup(message.getReplyMarkup());
        editMessageText.setChatId(message.getChatId());
        this.editMessageText = editMessageText;
    }
}
