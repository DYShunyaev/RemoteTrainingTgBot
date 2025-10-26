package d.shunyaev.RemoteTrainingTgBot;

import d.shunyaev.RemoteTrainingTgBot.components.CashComponent;
import d.shunyaev.RemoteTrainingTgBot.components.RegistrationComponent;
import d.shunyaev.RemoteTrainingTgBot.components.ValidateComponent;
import d.shunyaev.RemoteTrainingTgBot.utils.RandomUtils;
import d.shunyaev.model.RequestContainerCreateUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static d.shunyaev.RemoteTrainingTgBot.enums.ServicesUrl.CREATE_USER;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationTests {

    @InjectMocks
    private RegistrationComponent registrationComponent;
    @Mock
    private ValidateComponent validateComponent;
    private Message message = new Message();
    private long chatId;

    @BeforeEach
    public void setUp() {
        chatId = RandomUtils.generateRandomChatId();

        var user = new User();
        user.setId(chatId);
        user.setUserName("user_name");
        user.setFirstName("first_name");
        user.setLastName("last_name");

        message.setFrom(user);
    }

    @Test
    public void registrationPositiveTest() {
        when(validateComponent.isExistUser(anyLong())).thenReturn(true);

        var response = registrationComponent.registration(message, chatId);

        var expectedResponse = new SendMessage();
        var requestAfter = CashComponent.CREATE_USER_REQUESTS;

        String helloText = "helloText";
        expectedResponse.setChatId(chatId);
        expectedResponse.setText(helloText);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("РЕГИСТРАЦИЯ");
        button.setCallbackData(CREATE_USER.getUrl());

        keyboard.add(List.of(button));

        markupInLine.setKeyboard(keyboard);
        expectedResponse.setReplyMarkup(markupInLine);

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        expectedResponse, response
                ),
                () -> Assertions.assertEquals(
                        1, requestAfter.size()
                ),
                () -> Assertions.assertEquals(
                        new RequestContainerCreateUserRequest(), requestAfter.get(chatId)
                )
        );
    }

    @Test
    public void registrationNegativeTest() {
        when(validateComponent.isExistUser(anyLong())).thenReturn(false);

        var response = registrationComponent.registration(message, chatId);

        var expectedResponse = new SendMessage();
        var requestAfter = CashComponent.CREATE_USER_REQUESTS;

        expectedResponse.setChatId(chatId);
        expectedResponse.setText("Пользователь уже зарегестрирован.");

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        expectedResponse, response
                ),
                () -> Assertions.assertEquals(
                        0, requestAfter.size()
                )
        );
    }


}
