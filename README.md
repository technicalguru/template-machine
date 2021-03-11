# template-machine
Generate files based on directory-structured templates (supporting i18n and defaults with overriding)

## Synopsis
This application allows you to generate a complex tree of files based on [Freemarker](https://freemarker.apache.org/) templates. The very specific of this application is
that configuration, localizations and templates can be overridden in sub-folders of the tree as well as default language values and language aliases can be defined.

## Status and Roadmap
The project is stable. APIs are will not change in major versions. I use it a productive setup at a larger corporate to produce email templates in various languages and for various subsidiaries in a large scale.

## Features
* Generates a tree of files based on a tree of templates
* Provides localization, default localization values and language aliases
* Sub-folders can override configurations, localizations and templates
* CLI interface and programmatic API to control the generation

## Maven Coordinates

```
<dependency>
	<groupId>eu.ralph-schuster</groupId>
	<artifactId>template-machine</artifactId>
	<version>1.0.2</version>
</dependency>
```

## Documentation

* [Getting Started](doc/GETTING-STARTED.md)
* [Understanding the Concept](doc/CONCEPT.md)
* [Configuration](doc/CONFIGURATION.md)
* [CLI - Command Line Interface](doc/CLI.md)
* [Using the Java API](doc/JAVA-API.md)

## API Reference

API Javadoc for all versions is available via [javadoc.io](https://www.javadoc.io/doc/eu.ralph-schuster/template-machine).

## License

*template-machine* is free software: you can redistribute it and/or modify it under the terms of version 3 of the [GNU 
Lesser General Public License](LICENSE.md) as published by the Free Software Foundation.

*template-machine* is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public 
License for more details.

You should have received a copy of the GNU Lesser General Public License along with *template-machine*.  If not, see 
[https://www.gnu.org/licenses/lgpl-3.0.html](https://www.gnu.org/licenses/lgpl-3.0.html).

Summary:
 1. You are free to use all this code in any private or commercial project. 
 2. You must distribute license and author information along with your project.
 3. You are not required to publish your own source code.
 
## Contributions

Report a bug, request an enhancement or pull request at the [GitHub Issue Tracker](https://github.com/technicalguru/template-machine/issues). 
Make sure you have checked out the [Contribution Guideline](CONTRIBUTING.md)
