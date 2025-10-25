package d.shunyaev.RemoteTrainingTgBot.enums;

public enum ServicesUrl {
    CREATE_USER("createUser/"),
    EMAIL("email/"),
    WEIGHT("weight/"),
    HEIGHT("height/"),
    DATE_OF_BIRTH("dateOfBirth/"),
    LAST_NAME("lastName/")
    ;
    private final String url;
    ServicesUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
