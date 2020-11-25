# Getting Started

*template-machine* was developed on the idea that there are sophisticated document trees to be generated out of a few source documents. This can include:

* Support multiple languages
* Support any type of text document, especially HTML and TEXT
* Support different generations in various parts of a document tree, such as:
    * different wordings
    * different languages
    * different templates

*template-machine* will start a FreeMarker generation process that is organized hierarchically in a filesystem tree. Higher-level folders define
settings for their sub-folders, sub-folders can override settings easily. 

# The Basics

The source of a generation is a filesystem tree that already looks similar to what needs to be generated. Let's take an example. We want to
generate the following **target tree** structure:

```
                                                           (root)
                                                             |
                                  +--------------------------+--------------------------+
                                  |                                                     |
                               projectA                                              projectB
                                  |                                                     |
                 +----------------+----------------+                            +-------+-------+
                 |                                 |                            |               |
            subproject1                       subproject2                       de              en
                 |                                 |                            |               |
        +--------+------+                 +--------+------+                     +- file5.html   +- file5.html
        |               |                 |               |                     +- file6.txt    +- file6.txt
        de              en                de              en
        |               |                 |               |
        +- file1.html   +- file1.html     +- file3.html   +- file3.html
        +- file2.txt    +- file2.txt      +- file4.txt    +- file4.txt
```

This tree structure has basically 6 files to be generated in 2 languages. Now we want to create our **source tree**. We will strip off the
language directories to create our template files:

```
                                                           (root)
                                                             |
                                  +--------------------------+--------------------------+
                                  |                                                     |
                               projectA                                              projectB
                                  |                                                     |
                 +----------------+----------------+                                    +- file5.html
                 |                                 |                                    +- file6.txt
            subproject1                       subproject2 
                 |                                 |
                 +- file1.html                     +- file3.html
                 +- file2.txt                      +- file4.txt
```

We can now start to define language files at each level in this tree. Language file will be placed in a special
folder `__localization`. There are only 2 languages in our example. So we assume that we can write all English
translations in one file, and all German translations in another. We put these files on top level:

```
                                                           (root)
                                                             |
                        +---------------------------------------+--------------------------+
                        |                                       |                          |
                     projectA                                projectB               __localization
                        |                                       |                          |
       +----------------+----------------+                      +- file5.html              +- de.properties
       |                                 |                      +- file6.txt               +- en.properties
  subproject1                       subproject2 
       |                                 |
       +- file1.html                     +- file3.html
       +- file2.txt                      +- file4.txt
```

Such a language file is a normal Java Properties file. We give the various text fragments individual keys, e.g.

```
# English translations: en-properties
# -----------------------------------
greeting = Welcome guest,
apology = We feel sorry for the delay of your order and apologize hereby.
signature = Best Regards, your Company team
```

Our files `file1.html`, ..., `file6.txt` can now be rewritten to refer to these text fragments:

```
# This is file6.txt
####################

${greeting},

${apology}

${signature}
```

You may already recognize the FreeMarker template syntax. And in fact it is. Your files are actually FreeMarker templates now.

Once you start generation on this tree, it will produce the file6.txt in German and in English translation at the resepctive
subfolders.

