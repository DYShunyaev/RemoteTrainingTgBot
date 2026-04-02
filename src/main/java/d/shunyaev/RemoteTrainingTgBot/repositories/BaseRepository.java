package d.shunyaev.RemoteTrainingTgBot.repositories;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseRepository {

    protected final JdbcTemplate jdbcTemplate;

    protected BaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
