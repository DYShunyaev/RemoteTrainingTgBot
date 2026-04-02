package d.shunyaev.RemoteTrainingTgBot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UsersBot {

    private long id;
    private long chatId;
    private long userId;
    private String userName;
    private String firstName;
    private String lastName;
}
