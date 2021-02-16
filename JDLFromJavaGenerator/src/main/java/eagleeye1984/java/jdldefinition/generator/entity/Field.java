package eagleeye1984.java.jdldefinition.generator.entity;

public class Field {
    private final String name;
    private final FieldType fieldType;

    public Field(String name, FieldType fieldType) {
        this.name = transformName(name);
        this.fieldType = fieldType;
    }

    public String getName() {
        return name;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    private String transformName(String type) {
        return type.replaceAll("_", "");
    }
}
