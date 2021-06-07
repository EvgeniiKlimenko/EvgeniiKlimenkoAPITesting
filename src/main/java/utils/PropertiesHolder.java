package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesHolder {

    private static final String PATH_TO_PROPERTIES = "src/main/resources/SpellerProperties.properties";
    public static final Properties PROPS;

    static {
        PROPS = new Properties();
        try {
            PROPS.load(new FileInputStream(PATH_TO_PROPERTIES));
        } catch (IOException badEvent) {
            System.out.println("Can't load properties file!");
            badEvent.printStackTrace();
        }
    }

    public static Properties getProps() {
        return PROPS;
    }

}
