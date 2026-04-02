package d.shunyaev.RemoteTrainingTgBot.enums;

import jakarta.ws.rs.NotFoundException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MuscleGroup {
    CHEST("Грудные мышцы"),
    BACK("Мышцы спины"),
    LEGS("Мышцы ног"),
    TRICEPS("Трицепсы"),
    BICEPS("Бицепсы"),
    SHOULDERS("Плечи"),
    CARDIO("Кардио"),
    GENERAL("Общий комплекс"),
    OTHER("Другое");

    private final String description;

    MuscleGroup(String description) {
        this.description = description;
    }

    public static MuscleGroup getByDescription(String description) {
        return Arrays.stream(MuscleGroup.values())
                .filter(val -> val.getDescription().equals(description))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Не удалось найти значение с описанием %s"
                        .formatted(description)));
    }

}
