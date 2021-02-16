package eagleeye1984.java.jdldefinition.generator.entity;

import java.util.ArrayList;
import java.util.List;

public class FieldType {
    private final String name;
    private final List<String> typeArguments = new ArrayList<>();

    public FieldType(String name) {
        this.name = transformPrimitiveType(name);
    }

    public String getName() {
        return name;
    }

    public List<String> getTypeArguments() {
        return typeArguments;
    }

    private String transformPrimitiveType(String type) {
        switch (type) {
            case "int":
                return "Int";

            case "float":
                return "Float";

            case "boolean":
                return "Boolean";

            default:
                return transformName(type);
        }
    }

    /**
     * If there is "." take part after last "." because this is the class name.
     *
     * @param name Type name.
     * @return Type name.
     */
    public static String transformName(String name) {
        if (name.contains(".")) {
            name = name.substring(name.lastIndexOf(".") + 1);
        }

        return name;
    }
}
