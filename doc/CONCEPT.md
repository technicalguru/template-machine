# Understanding template-machine's Concept

Now, that you understood how templates and language files are organized, it is time to go more into depth and understand
how a template is being constructed.

# Language Files
As shown in [Getting Started](Getting Started), language file are organized in individual Java Properties files in a 
special folder named `__localization`.

This folder can appear at each level in your source tree and will define the language keys only for its sub-folders.
This means that language file can overwrite definitions from higher folders:

```
                                                                       (root)
                                                                         |
                        +------------------------------------------------+----------------------------------+
                        |                                                |                                  |
                     projectA                                         projectB                       __localization
                        |                                                |                                  |
       +----------------+----------------+                 +-------------+-------------+                    +- de.properties
       |                                 |                 |                           |                    +- en.properties
  subproject1                       subproject2            +- file5.html        __localization
       |                                 |                 +- file6.txt                |
       +- file1.html       +-------------+-------------+                               +- de.properties
       +- file2.txt        |                           |                               +- en.properties
                           +- file3.html        __localization
                           +- file4.txt                |
                                                       +- de.properties
                                                       +- en.properties
```

The files will now receive their language definitions from different language files:

* `/projectA/subproject1/` files will get definitions from `/__localization` folder
* `/projectA/subproject2/` files will get definitions from two folders: `/projectA/subproject2/__localization` and `/__localization`
* `/projectB2/` files will get definitions from two folders: `/projectB/__localization` and `/__localization`

Sub-folders overwrite and amend parent folder definitions. Or, any individual language key is looked up from bottom-to-top `__localization`
definitions.

# Default Language Files
It is very common that some definitions are equal across all languages. Instead of defining them in each individual language file, you can 
have a default definition that jumps in when a specific translation is not available in a language. Default translations are defined in
`default.properties` files and can be organized hierarchically as well:

```
                                                                       (root)
                                                                         |
                        +------------------------------------------------+----------------------------------+
                        |                                                |                                  |
                     projectA                                         projectB                       __localization
                        |                                                |                                  |
       +----------------+----------------+                 +-------------+-------------+                    +- de.properties
       |                                 |                 |                           |                    +- en.properties
  subproject1                       subproject2            +- file5.html        __localization              +- default.properties
       |                                 |                 +- file6.txt                |
       +- file1.html       +-------------+-------------+                               +- de.properties
       +- file2.txt        |                           |                               +- en.properties
                           +- file3.html        __localization                         +- default.properties
                           +- file4.txt                |
                                                       +- de.properties
                                                       +- en.properties
                                                       +- default.properties
```

Please notice that a specific language key is searched first in the respective language files up to the top definition. If no such key exists then
the default definitions are searched from bottom to top.

# FreeMarker Macros

A common task in HTML generation is to generate the same HTML snippet at various places. This snippet can be externalized into another
file and made available to other template files, which in turn import them. FreeMarker calls them macros. And here is what such a macro file could looks like:

```
<#macro mysnippet>
   <a href="https://www.example.com"><img href="/mylogo.gif"></a>
</#macro>
```

*template-machine* fully supports macro files and makes them available to your template. You just need to put the macro file in a special folder 
named `__templates`. The directory tree would now look like this:

```
                                                                       (root)
                                                                         |
                        +------------------------------------------------+----------------------------------+------------------------------+
                        |                                                |                                  |                              |
                     projectA                                         projectB                       __localization                  __templates
                        |                                                |                                  |                              |
       +----------------+----------------+                 +-------------+-------------+                    +- de.properties               +- my-macros.ftl
       |                                 |                 |                           |                    +- en.properties               +- more-macros.ftl
  subproject1                       subproject2            +- file5.html        __localization              +- default.properties
       |                                 |                 +- file6.txt                |
       +- file1.html       +-------------+-------------+                               +- de.properties
       +- file2.txt        |                           |                               +- en.properties
                           +- file3.html        __localization                         +- default.properties
                           +- file4.txt                |
                                                       +- de.properties
                                                       +- en.properties
                                                       +- default.properties
```

You can now import your macros into your template files:

```
# This is file6.txt
####################
<#import "my-macros.ftl" as myMacros>

${greeting},

${apology}

${signature}

<@myMacros.mysnippet/>
```

As with language files, macro files can be overwritten by sub-folders. However, unlike language definitions that amend the parent definitions,
macro files are **replaced** by sub-folder macro files of the same name. A specific macro file is searched from bottom to top. There is no
such thing like default macro definitions.

