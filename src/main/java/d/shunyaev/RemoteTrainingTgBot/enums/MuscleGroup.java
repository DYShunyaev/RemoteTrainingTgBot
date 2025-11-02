package d.shunyaev.RemoteTrainingTgBot.enums;

import lombok.Getter;

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

}
