package ctu.via.bonvoyage.interfaces.enums;

public enum CategoryEnum {

    FOOD( "Jídlo a pití", "100-1000-0000,100-1000-0001,100-1000-0002,100-1000-0008,100-1000-0009"),
    ENTERTAINMENT( "Zábava", "200-2000-0014,200-2000-0015,200-2000-0016,200-2000-0017,200-2100,200-2200"),
    MONUMENTS( "Památky a muzea", "300-3000,300-3100,300-3200"),
    ACCOMMODATION("Ubytování", "500-5000");

    private final String code;
    private final String value;


    CategoryEnum(String value, String code) {
        this.code = code;
        this.value = value;
    }

    public static String fromValue(String value) {
        for (CategoryEnum category : values()) {
            if (category.value.equals(value)){
                return category.code;
            }
        }
        return "";
    }

    public String getCode() {
        return code;
    }

}