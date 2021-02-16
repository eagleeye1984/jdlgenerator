package eagleeye1984.java.jdldefinition.generator.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Entity {
    private final EntityType entityType;
    private String fullName;
    private final String name;
    private final List<Field> fields = new ArrayList<>();

    public Entity(EntityType entityType, String name) {
        this.entityType = entityType;
        this.name = name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Entity)) {
            return false;
        }

        Entity entity = (Entity) o;

        return Objects.equals(name, entity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
