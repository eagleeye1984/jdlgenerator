package eagleeye1984.java.jdldefinition.generator;

import eagleeye1984.java.jdldefinition.generator.jdl.JavaJDLGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Start {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("At least one parameter is necessary!");
            System.out.println("Input parameters (* - required):");
            System.out.println(" - *Input file or folder containing *.java files");
            System.out.println(" - Output file (if entered) where JDL definition will be printed");
        } else {
            Path outputFile = args.length > 1 ? Paths.get(args[1]) : null;

            JavaJDLGenerator javaJDLGenerator = new JavaJDLGenerator();
            javaJDLGenerator.createJDLdefinition(Paths.get(args[0]), outputFile);
        }
    }
}
