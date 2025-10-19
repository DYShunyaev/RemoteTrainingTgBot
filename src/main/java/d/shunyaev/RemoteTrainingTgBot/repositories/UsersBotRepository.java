package d.shunyaev.RemoteTrainingTgBot.repositories;

import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UsersBotRepository extends BaseRepository{

    @Autowired
    public UsersBotRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void setNewUser(UsersBot user) {
        String sql = """
                insert into users_bot (chat_id, user_name, first_name, last_name)
                values (?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                user.getChatId(),
                user.getUserName(),
                user.getFirstName(),
                user.getLastName());
    }

    public UsersBot getUserBotByChatId(long chatId) {
        String sql = """
                select * from users_bot
                where chat_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, mapToRowToUser(), chatId);
    }

    private RowMapper<UsersBot> mapToRowToUser() {
        return ((rs, rowNum) -> new UsersBot()
                .setId(rs.getLong("id"))
                .setChatId(rs.getLong("chat_id"))
                .setUserName(rs.getString("user_name"))
                .setFirstName(rs.getString("first_name"))
                .setLastName(rs.getString("last_name")));
    }
}
