# Command Line Interface

The *template-machine* command-line interface is pretty simple. it offers you a way to configure all aspects of the
generation process.

## The Distribution package

In case you have a copy of the distribution archive, untar this anywhere in your filesystem and go to `bin` directory.
There you will find the `run.bat` (Windows Shell) and `run.sh` (Linux Bash Shell) scripts which you
can start right away without any arguments.

## The Java Execution
You will need all dependencies and the main template-machine.jar file in your Java classpath. The Java Runtime
Environment (min JavaSE-9) shall be available in your path. Then you can start *template-machine* as follows:

```
# Linux
java -classpath $CLASSPATH templating.TemplateMachine [<arguments>]

# Windows
java.exe -classpath %CLASSPATH% templating.TemplateMachine [<arguments>]
```

## File encoding
Please notice that file-encoding can be tricky with Java. You might need to add the `-Dfile.encoding=UTF-8` parameter
in your call (replace the charset name if required).

## Command-Line Arguments

Starting the *template-machine* without any arguments will give you a short help text:

```
usage: template-machine
 -c,--config <arg>           configuration file (optional, defaults to template-machine.properties)
 -f,--force                  overwrite existing output directory (optional)
 -o,--output-dir <arg>       output directory (optional)
 -r,--read-encoding <arg>    encoding of templates (optional, defaults to platform)
 -s,--sub-dir <arg>          sub directory to generate within project (optional)
 -t,--template-dir <arg>     (template) source directory
 -w,--write-encoding <arg>   encoding for generates files (optional, defaults to platform)
```

A minimum call would require option `-t` which defines the template directory.

The output directory is named from the source directory but with ending `.generated`. If such a directory
already exists then the generation will abort with an error message. However, you can force the generation
with the `-f` argument.

Character encodings are defined with arguments `-r` (for reading your source tree) and `-w` for
writing the generated files. The default values are platform specific and usually CP-1252 on Windows
and UTF-8 on Linux machines. You shall always give character encodings explicitly in order to make
your generation repeatable.

Argument `-s` can help you to save time while you are working on your source files. It is a relative
directory name to your root source folder. Only this sub-tree will be generated then.
