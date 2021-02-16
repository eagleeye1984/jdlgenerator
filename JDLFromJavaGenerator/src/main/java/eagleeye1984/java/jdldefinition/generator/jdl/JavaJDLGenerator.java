package eagleeye1984.java.jdldefinition.generator.jdl;

import eagleeye1984.java.jdldefinition.generator.entity.Entity;
import eagleeye1984.java.jdldefinition.generator.entity.EntityType;
import eagleeye1984.java.jdldefinition.generator.entity.Field;
import eagleeye1984.java.jdldefinition.generator.entity.FieldType;
import eagleeye1984.java.jdldefinition.generator.entity.PrimitiveTypeEnum;
import eagleeye1984.java.jdldefinition.generator.antlr4.generated.Java8Lexer;
import eagleeye1984.java.jdldefinition.generator.antlr4.generated.Java8Parser;
import eagleeye1984.java.jdldefinition.generator.listener.Java8CustomListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaJDLGenerator {
    private static final Logger logger = LogManager.getLogger(JavaJDLGenerator.class);
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String INDENTATION = "  ";
    private static final List<String> collectionTypes = new ArrayList<>();
    private static final DirectoryStream.Filter<Path> filter = file -> (Files.isDirectory(file) || file.toFile().getName().endsWith(".java"));

    static {
        collectionTypes.add("List");
        collectionTypes.add("Collection");
        collectionTypes.add("Set");
        collectionTypes.add("Map");
        collectionTypes.add("HashMap");
    }

    /**
     * Scan given path for java classes and generate JDL for all of them. The JDL definition is printed to console and to
     * <code>outputFile</code> if not null.
     *
     * @param path       Folder/file containing *.java files.
     * @param outputFile Output file where JDL should be printed to. If null only to console the JDL definition is printed.
     */
    public void createJDLdefinition(Path path, Path outputFile) throws IOException {
        List<Path> classFiles = new ArrayList<>();
        listAllClasses(classFiles, path);

        Map<String, Entity> entities = parseJavaClasses(classFiles);

        printEntities(entities);
        printJDLDefinition(entities, outputFile);
    }

    /**
     * Find all files on given path.
     *
     * @param foundFiles Output list of java classes.
     * @param root       File or root folder.
     */
    private void listAllClasses(List<Path> foundFiles, Path root) {
        if (root.toFile().isFile()) {
            foundFiles.add(root);
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, filter)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        listAllClasses(foundFiles, path);
                    } else {
                        foundFiles.add(path);
                    }
                }
            } catch (AccessDeniedException e) {
                logger.error("Access denied to directory {}", root, e);
            } catch (IOException e) {
                logger.error("Error while reading directory {}", root, e);
            }
        }
    }

    /**
     * Parse given files using ANTLR and create entities. Entities contain definition fo entities/enums for JDL definition.
     *
     * @param classFiles List fo java files to be parsed.
     * @return Entities contain definition fo entities/enums for JDL definition.
     */
    private Map<String, Entity> parseJavaClasses(List<Path> classFiles) {
        logger.trace("parseJavaClass");

        // Instantiate walker and listener
        ParseTreeWalker walker = new ParseTreeWalker();
        Java8CustomListener listener = new Java8CustomListener();

        classFiles.forEach(path -> {
            logger.trace("========== Parse: " + path.toFile().getName() + " ==========");

            try {
                ParseTree tree = getTree(path);

                // walk through class
                walker.walk(listener, tree);
            } catch (IOException e) {
                logger.error("Error while reading creating tree for: {}", path, e);
            }
        });

        return listener.getEntityMap();
    }

    /**
     * Print given entities.
     *
     * @param entities Entities created from *.java files.
     */
    private void printEntities(Map<String, Entity> entities) {
        // Print created entities (debug)
        logger.debug("");
        logger.debug("Loaded entities: ");

        entities.values().forEach(entity -> {
            EntityType type = entity.getEntityType();
            logger.debug((EntityType.CLASS.equals(type) ? "entity" : "enum") + " " + entity.getName());

            entity.getFields().forEach(field -> {
                if (EntityType.ENUM.equals(type)) {
                    logger.debug(INDENTATION + field.getName());
                } else {
                    if (field.getFieldType() != null) {
                        logger.debug(INDENTATION + field.getName() + " " + field.getFieldType().getName());
                        field.getFieldType().getTypeArguments().forEach(arg -> logger.debug("    " + arg));
                    }
                }
            });
        });
    }

    private void printJDLDefinition(Map<String, Entity> entitiesMap, Path outputFile) throws IOException {
        logger.debug("");
        logger.debug("");
        logger.debug("Print JDL definition");

        List<String> oneToOne = new ArrayList<>();
        List<String> manyToOne = new ArrayList<>();

        StringBuilder jdlDefinition = new StringBuilder();

        for (Entity entity : entitiesMap.values()) {
            EntityType type = entity.getEntityType();

            jdlDefinition.append(EntityType.CLASS.equals(type) ? "entity" : "enum")
                         .append(" ")
                         .append(entity.getName())
                         .append(" {")
                         .append(LINE_SEPARATOR);

            if (EntityType.ENUM.equals(type)) {
                jdlDefinition.append("  ").append(entity.getFields().stream().map(Field::getName).collect(Collectors.joining(", ")));
            } else {
                logger.debug(entity.getName() + ":");

                jdlDefinition.append(entity.getFields()
                                           .stream()
                                           .peek(field -> {
                                               // Prepare one-to-one and one-to-many relationships
                                               if (field.getFieldType() != null) {
                                                   if (!field.getFieldType().getTypeArguments().isEmpty()) {
                                                       // It is collection, add it to manyToOne
                                                       field.getFieldType().getTypeArguments()
                                                            .stream()
                                                            .filter(typeArg -> !PrimitiveTypeEnum.isPrimitiveType(typeArg) || isCollection(typeArg))
                                                            .filter(typeArg -> isEntityTypeClass(entitiesMap, typeArg))
                                                            .collect(Collectors.toList())
                                                            .forEach(s -> manyToOne.add(INDENTATION + s + " to " + entity.getName() + "{" + field.getName() + "}"));
                                                   } else if (!PrimitiveTypeEnum.isPrimitiveType(field.getFieldType().getName())) {
                                                       if (isEntityTypeClass(entitiesMap, field.getFieldType().getName())) {
                                                           // It is some class instance, add one to one
                                                           oneToOne.add(INDENTATION + entity.getName() + "{" + field.getName() + "} to " + field.getFieldType().getName());
                                                       }
                                                   }
                                               } else {
                                                   logger.debug("There is field without type defined!!! " + entity.getName() + "{" + field.getName() + "}");
                                               }
                                           })
                                           .filter(field -> !isEntityTypeClass(entitiesMap, getCollectionArgument(entitiesMap, field.getFieldType())))
                                           .map(field -> INDENTATION + field.getName() + " " + getCollectionArgument(entitiesMap, field.getFieldType()))
                                           .collect(Collectors.joining("," + LINE_SEPARATOR)));
            }

            jdlDefinition.append(LINE_SEPARATOR)
                         .append("}")
                         .append(LINE_SEPARATOR)
                         .append(LINE_SEPARATOR);
        }

        if (!oneToOne.isEmpty()) {
            jdlDefinition.append("relationship OneToOne {")
                         .append(LINE_SEPARATOR)
                         .append(oneToOne.stream().collect(Collectors.joining("," + LINE_SEPARATOR)))
                         .append(LINE_SEPARATOR)
                         .append("}")
                         .append(LINE_SEPARATOR)
                         .append(LINE_SEPARATOR);
        }

        if (!manyToOne.isEmpty()) {
            jdlDefinition.append("relationship OneToMany {")
                         .append(LINE_SEPARATOR)
                         .append(manyToOne.stream().collect(Collectors.joining("," + LINE_SEPARATOR)))
                         .append(LINE_SEPARATOR)
                         .append("}")
                         .append(LINE_SEPARATOR)
                         .append(LINE_SEPARATOR);
        }

        System.out.println("JDL definition:");
        System.out.println(jdlDefinition.toString());

        printJDLToFile(outputFile, jdlDefinition);
    }

    /**
     * Is given entity type name CLASS?
     *
     * @param entitiesMap Map of all entities.
     * @param typeName    Entity type name.
     * @return <code>True</code> if given entity type name belongs to CLASS; <code>false</code> otherwise.
     */
    private boolean isEntityTypeClass(Map<String, Entity> entitiesMap, String typeName) {
        Entity typeEntity = entitiesMap.get(typeName);
        return typeEntity != null && EntityType.CLASS.equals(typeEntity.getEntityType());
    }

    /**
     * Print JDL definition to given file (if not null).
     *
     * @param outputFile    Output file.
     * @param jdlDefinition JDL definition.
     * @throws IOException
     */
    private void printJDLToFile(Path outputFile, StringBuilder jdlDefinition) throws IOException {
        if (outputFile != null) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.toFile()));
            writer.write(jdlDefinition.toString());
            writer.close();
        }
    }

    /**
     * If given entity type is collection type then get first argument which is from parsed entities. If none found return first one. Otherwise if given entity type is not
     * collection type, then return it's name.<br><br>
     * Only attributes from parsed entities are taken because non primitive types for collections are put to relationships.
     *
     * @param entitiesMap Map of parsed entities.
     * @param fieldType   Filed type.
     * @return Name of field type or collection attribute.
     */
    private static String getCollectionArgument(Map<String, Entity> entitiesMap, FieldType fieldType) {
        if (!fieldType.getTypeArguments().isEmpty()) {
            return fieldType.getTypeArguments()
                            .stream()
                            .map(FieldType::transformName)
                            .filter(entitiesMap::containsKey)
                            .findFirst()
                            .orElse(fieldType.getTypeArguments().get(0));
        } else {
            return fieldType.getName();
        }
    }

    /**
     * Is type name treated as collection type?
     *
     * @param typeName Type name.
     * @return <code>True</code> if given type name is treated as collection; <code>false</code> otherwise.6
     */
    private static boolean isCollection(String typeName) {
        logger.debug("isCollection: " + typeName);
        return collectionTypes.contains(typeName);
    }

    private static ParseTree getTree(Path filePath) throws IOException {
        // Construct lexer
        Java8Lexer lexer = new Java8Lexer(CharStreams.fromPath(filePath));

        // Instantiate parser
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);
        return parser.compilationUnit();
    }
}
