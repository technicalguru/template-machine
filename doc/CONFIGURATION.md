# Configuration

Configuration is read by default from a file `template-machine.properties` located in the root folder of your
source tree. However, several other options exist:

* You can pass a command-line parameter to read the file from another location (see [CLI - Command Line Interface](CLI.md))
* You can override the main configuration file by folder-specific configuration files at each folder in your tree. These
  files can contain the same configuration directives but relate to the current directory and its subfolders only. These
  files must be named `.config`
* You can programmatically configure the root `Context` when you integrate *template-machine* into your program (see
  [Java API](JAVA-API.md))

# Configuration Directives

## Languages

The `languages` keyword defines what languages will be generated from your files and how they are setup. It is
a comma-separated list of languages that you want to generate:

```
# Generate a few languages
languages = de,en,pt,tr,es,fr,it
```

You will need language files for each of these language codes.

You also can map language files, e.g. the following definition will generate Turkish from English definition:

```
# Map Turkish to English
languages = de,en,pt,tr=en,es,fr,it
```

or map a specific country-specific code from the general definition:

```
# Map country-specific from general definition
languages = de,en-UK=en,pt,tr,es,fr,it
```

*template-machine* can detect the languages from existing language files. This is the default:

```
# Default (auto-detection)
languages = auto
```

Or you can combine the auto-detection with the mapping. This will use the `other` keyword:

```
# generate German, English, Portuguese and any other language that can be detected
languages = de,en-UK=en,pt,other
```

## Location of Language Files

Language files are usually searched in `__localization` folder within your source tree. However,
you can modify this name:

```
# Use a different localization folder
localizationDir = myLocalizationFiles
```

Please notice that this is a single, relative directory name because it will be used at all directory 
levels within your source tree.

## Location of FreeMarker Macro files

FreeMarker macro files are usually searched in `__templates` folder within your source tree. However,
you can modify this name:

```
# Use a different localization folder
templateDir = myMacros
```

Please notice that this is a single, relative directory name because it will be used at all directory 
levels within your source tree.

