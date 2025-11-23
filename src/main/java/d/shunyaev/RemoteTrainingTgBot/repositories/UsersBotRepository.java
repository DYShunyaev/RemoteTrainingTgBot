package d.shunyaev.RemoteTrainingTgBot.repositories;

import d.shunyaev.RemoteTrainingTgBot.models.UsersBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

        String sqlRegistr = """
                insert into user_registration (chat_id, registration)
                values (?, 0)
                """;

        jdbcTemplate.update(sql,
                user.getChatId(),
                user.getUserName(),
                user.getFirstName(),
                user.getLastName());
        jdbcTemplate.update(sqlRegistr,
                user.getChatId());
    }

    public void setUserId(long userId, String userName) {
        String sql = """
                update users_bot set user_id = ?
                where user_name = ?
                """;
        jdbcTemplate.update(sql, userId, userName);
    }

    public void setFlagRegistration(long chatId) {
        String sql = """
                update user_registration set registration = 1
                where chat_id = ?
                """;
        jdbcTemplate.update(sql, chatId);
    }

    public Integer getRegistrationFlagByChatId(long chatId) {
        String sql = """
                select registration from user_registration
                where chat_id = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public UsersBot getUserBotByChatId(long chatId) {
        String sql = """
                select * from users_bot
                where chat_id = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, mapToRowToUser(), chatId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public UsersBot getUserBotByUserName(String userName) {
        String sql = """
                select * from users_bot
                where user_name = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, mapToRowToUser(), userName);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private RowMapper<UsersBot> mapToRowToUser() {
        return ((rs, rowNum) -> new UsersBot()
                .setId(rs.getLong("id"))
                .setChatId(rs.getLong("chat_id"))
                .setUserId(rs.getLong("user_id"))
                .setUserName(rs.getString("user_name"))
                .setFirstName(rs.getString("first_name"))
                .setLastName(rs.getString("last_name")));
    }
}
