package eagleeye1984.java.jdldefinition.generator.entity;

import java.util.Arrays;

/**
 * Enum for primitive types which doesn't need to have one-to-one relationship in JDL.
 */
public enum PrimitiveTypeEnum {
    BYTE("byte"),
    BYTE_WRAPPER("Byte"),
    SHORT("short"),
    SHORT_WRAPPER("Short"),
    INTEGER("int"),
    INTEGER_WRAPPER("Integer"),
    LONG("long"),
    LONG_WRAPPER("Long"),
    FLOAT("float"),
    FLOAT_WRAPPER("Float"),
    DOUBLE("double"),
    DOUBLE_WRAPPER("Double"),
    CHAR("char"),
    STRING("String"),
    BOOLEAN("boolean"),
    BOOLEAN_WRAPPER("Boolean");

    private final String value;

    PrimitiveTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isPrimitiveType(String typeName) {
        return Arrays.stream(values()).map(PrimitiveTypeEnum::getValue)
              .filter(name -> name.equals(typeName))
              .findFirst()
              .orElse(null) != null;
    }
}
