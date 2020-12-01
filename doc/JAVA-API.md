# Using the Java API

*template-machine* can be integrated programatically in any Java application. This page demonstrates
how to achieve this.

## Generating the Root Context

Conceptually, each directory in a template source tree can have its own configuration. *template-machine*
calls this a context. The Java class `Context` is the respective representation of such a 
configuration. That's why you will need to create the root context before you can start the engine:

```
File sourceDir           = new File("/path/to/my/source/folder");
File targetDir           = new File("/path/to/my/output/folder");
File subDir              = new File(sourceDir, "my/sub/directory");
Properties configuration = new Properties();
// fill the configuration object here...

Context rootContext = new Context(sourceDir, targetDir, subDir, configuration);
```

The `configuration` Properties object is a representation of the [Configuration](CONFIGURATION.md) 
parameters. It can be empty if you want to use the defaults.

Optionally, you can specify the reading and writing character encodings:

```
Charset readEncoding = Charset.forName("UTF-8");
rootContext.setReadEncoding(readEncoding);

Charset writeEncoding = Charset.forName("UTF-8");
rootContext.setWriteEncoding(writeEncoding);
```

And, when there are files within your tree that you want to skip for generation:

```
rootContext.ignoreFile(anyFileObject);
rootContext.ignoreFile(anotherFileObject);
```

## Creating and Running the Template Machine

Now you can create an run *template-machine*:

```
TemplateMachine machine = new TemplateMachine(rootContext);
machine.generate();
```

That's it. Simple and straight forward.

## Java API Documentation

The Java API can be found here: [https://javadoc.io/doc/eu.ralph-schuster/template-machine](https://javadoc.io/doc/eu.ralph-schuster/template-machine)
