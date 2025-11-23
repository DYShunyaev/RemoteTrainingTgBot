package d.shunyaev.RemoteTrainingTgBot.enums;

public enum ServicesUrl {

    START("start"),
    CREATE_USER("createUser/"),
    EMAIL("email/"),
    WEIGHT("weight/"),
    HEIGHT("height/"),
    DATE_OF_BIRTH("dateOfBirth/"),
    LAST_NAME("lastName/"),
    CREATE_NEW_TRAINING("createNewTraining/"),
    GENERATE_NEW_TRAINING("generateNewTraining/"),
    UPDATE_TRAINING("updateTraining/"),
    CREATE_NEW_EXERCISE("createNewExercise/"),
    SET_MY_TRAINER("setMyTrainer/")
    ;
    private final String url;
    ServicesUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
