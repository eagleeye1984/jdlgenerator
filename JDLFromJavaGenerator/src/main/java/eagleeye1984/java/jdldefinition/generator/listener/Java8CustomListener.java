package eagleeye1984.java.jdldefinition.generator.listener;

import eagleeye1984.java.jdldefinition.generator.entity.Entity;
import eagleeye1984.java.jdldefinition.generator.entity.EntityType;
import eagleeye1984.java.jdldefinition.generator.entity.Field;
import eagleeye1984.java.jdldefinition.generator.entity.FieldType;
import eagleeye1984.java.jdldefinition.generator.antlr4.generated.Java8Parser;
import eagleeye1984.java.jdldefinition.generator.antlr4.generated.Java8ParserBaseListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listener for Java 8 ANTLR grammar. Listens to picked parts of Java 8 classes and prepares entities which will be used for generating JDL definition.<br>
 * Not all parts of Java 8 class is listened, only those needed for JDL definition generation.
 */
public class Java8CustomListener extends Java8ParserBaseListener {
    private static final Logger logger = LogManager.getLogger(Java8CustomListener.class);
    private final Map<String, Entity> entityMap = new HashMap<>();
    private final LinkedList<Entity> currentEntities = new LinkedList<>();
    private final LinkedList<Entity> currentEnums = new LinkedList<>();
    private final AtomicInteger methodDeepLevel = new AtomicInteger(0);
    private FieldType currentFieldType = null;

    private Entity getLastEntity() {
        if (currentEntities.isEmpty()) {
            return null;
        } else {
            return currentEntities.getLast();
        }
    }

    private Entity getLastEnum() {
        if (currentEnums.isEmpty()) {
            return null;
        } else {
            return currentEnums.getLast();
        }
    }

    private Entity removeLastEntity() {
        if (currentEntities.isEmpty()) {
            return null;
        } else {
            return currentEntities.removeLast();
        }
    }

    private Entity removeLastEnum() {
        if (currentEnums.isEmpty()) {
            return null;
        } else {
            return currentEnums.removeLast();
        }
    }

    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        if (ctx.normalClassDeclaration() != null) {
            TerminalNode identifier = ctx.normalClassDeclaration().Identifier();
            log("enterClassDeclaration: " + identifier.getText());

            Entity entity = new Entity(EntityType.CLASS, identifier.getText());

            currentEntities.add(entity);
            entityMap.put(entity.getName(), entity);
        }
    }

    @Override
    public void exitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        if (ctx.normalClassDeclaration() != null) {
            TerminalNode identifier = ctx.normalClassDeclaration().Identifier();
            String className = identifier.getText();
            log("exitClassDeclaration: " + className);

            removeLastEntity();
        }
    }

    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        methodDeepLevel.incrementAndGet();
    }

    @Override
    public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        methodDeepLevel.decrementAndGet();
    }

    @Override
    public void enterUnannType(Java8Parser.UnannTypeContext ctx) {
        log("enterUnannType: " + ctx.getText());
        currentFieldType = new FieldType(ctx.getText());
        log("  >>set currentFieldType " + currentFieldType.getName());
    }

    @Override
    public void enterTypeArguments(Java8Parser.TypeArgumentsContext ctx) {
        log("enterTypeArguments");
        log("  currentFieldType: " + currentFieldType.getName());

        ctx.typeArgumentList()
           .typeArgument()
           .forEach(typeArgumentContext -> {
               String type = null;

               if (typeArgumentContext.referenceType() != null) {
                   type = typeArgumentContext.referenceType()
                                             .classOrInterfaceType()
                                             .classType_lfno_classOrInterfaceType()
                                             .Identifier().getText();
               } else if (typeArgumentContext.wildcard() != null && typeArgumentContext.wildcard().wildcardBounds() != null) {
                   type = typeArgumentContext.wildcard().wildcardBounds().referenceType()
                                             .classOrInterfaceType()
                                             .classType_lfno_classOrInterfaceType()
                                             .Identifier().getText();
               }

               log("  typeArgument: " + type);

               if (type != null) {
                   currentFieldType.getTypeArguments().add(type);
               }
           });
    }

    @Override
    public void enterVariableDeclarator(Java8Parser.VariableDeclaratorContext ctx) {
        if (methodDeepLevel.get() == 0) {
            TerminalNode identifier = ctx.variableDeclaratorId().Identifier();
            log("enterVariableDeclarator: " + identifier.getText());

            if (currentFieldType != null) {
                log("    currentFieldType: " + currentFieldType.getName());
            }

            Entity currentEntity = getLastEntity();
            // Enum variables are not necessary
//            Entity currentEnum = getLastEnum();

            if (currentEntity != null) {
                log("    currentEntity: " + currentEntity.getName());
                currentEntity.getFields().add(new Field(identifier.getText(), currentFieldType));
            }/* else if (currentEnum != null) {
                log("    currentEnum: " + currentEnum);
                currentEnum.getFields().add(new Field(identifier.getText(), currentFieldType));
            }*/

            currentFieldType = null;
        }
    }

    @Override
    public void enterEnumDeclaration(Java8Parser.EnumDeclarationContext ctx) {
        TerminalNode identifier = ctx.Identifier();
        log("enterEnumDeclaration: " + identifier.getText());
        Entity currentEnum = new Entity(EntityType.ENUM, identifier.getText());

        currentEnums.add(currentEnum);
        entityMap.put(currentEnum.getName(), currentEnum);
    }

    @Override
    public void exitEnumDeclaration(Java8Parser.EnumDeclarationContext ctx) {
        TerminalNode identifier = ctx.Identifier();
        log("exitEnumDeclaration: " + identifier.getText());
        removeLastEnum();
    }

    @Override
    public void enterEnumConstantList(Java8Parser.EnumConstantListContext ctx) {
        log("enterEnumConstantList: " + ctx.getText());
        Entity currentEnum = getLastEnum();
        log("currentEnum: " + currentEnum);

        if (currentEnum != null) {
            ctx.enumConstant()
               .forEach(enumConstantContext -> {
                   log("    enumConstant: " + enumConstantContext.Identifier());
                   currentEnum.getFields().add(new Field(enumConstantContext.Identifier().getText(), null));
               });
        }
    }

    private void log(Object message) {
        logger.trace(message);
    }

    public Map<String, Entity> getEntityMap() {
        return entityMap;
    }
}
