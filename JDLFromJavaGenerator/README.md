# Intro
This is simple application which creates JDL definition from existing Java 8 files.

The JDL definition file can be used with [JHipster](https://www.jhipster.tech/) development platform or with [JDL-Studio](https://start.jhipster.tech/jdl-studio/).

This application is not intended for any kind of piracy. This application is intended for cases when you want to:
 - migrate your application to use [JHipster](https://www.jhipster.tech/) - for example to migrate your desktop application to web application using [Angular](https://angular.io/).
 - generate UML class diagram using [JDL-Studio](https://start.jhipster.tech/jdl-studio/) - for example to create documentation or analysis

# Input parameters
The application takes 2 input parameters (* required):
1. **Folder path or file name***
2. Output file name

## Folder path or file name
This parameter is required.
This can be path to folder where are multiple *.java classes (include sub-folders) or one *.java file.

## Output file name
This parameter is optional and if given the JDL definition is written into given file.

# Description
This application is really simple. It can be run from IntelliJ Idea and takes 2 input parameters.

To run this app from IntelliJ IDEA use Start.java as entry point (main class).

Application uses [ANTLR4](https://www.antlr.org/) and [Java8 grammar](https://github.com/antlr/codebuff/blob/master/grammars/org/antlr/codebuff/Java8.g4) to parse java classes.

# License
Feel free to use this base for any project you want.
