# Quill

A collection of Gradle plugins. Some of the plugins enhance existing functionality in Gradle whereas
others provide new functionality. All of the plugins can be used individually; they do not depend on
each other.

The Quill plugins are designed for use with Gradle 2.0 or later, except for the Checkstyle Additions
plugin, which requires Gradle 2.7 or later, and the Module Info plugin, which requires Gradle 4.2.1
or later. Any success in using the plugins with earlier versions of Gradle than 2.0 is purely
coincidental.

The Quill plugins were developed to support the author's way of working with Gradle. They may not
appeal to the taste of those who work in different ways.


## Contents
1. [Release Notes](#release-notes)
1. [General Usage](#general-usage)
1. [Ivy Import Plugin](#ivy-import-plugin)
1. [Maven Import Plugin](#maven-import-plugin)
1. [Project Metadata Plugin](#project-metadata-plugin)
1. [Java Additions Plugin](#java-additions-plugin)
1. [JUnit Additions Plugin](#junit-additions-plugin)
1. [JaCoCo Additions Plugin](#jacoco-additions-plugin)
1. [SpotBugs Additions Plugin](#spotbugs-additions-plugin)
1. [Checkstyle Additions Plugin](#checkstyle-additions-plugin)
1. [PMD Additions Plugin](#pmd-additions-plugin)
1. [CPD Plugin](#cpd-plugin)
1. [Scent Plugin](#scent-plugin)
1. [Reports Dashboard Plugin](#reports-dashboard-plugin)
1. [Pom Plugin](#pom-plugin)
1. [Module Info Plugin](#module-info-plugin)
1. [Cobertura Plugin](#cobertura-plugin)
1. [JDepend Additions Plugin](#jdepend-additions-plugin)


## Release Notes

### version 1.5

* Gradle 4 compatibility.
* [Module Info Plugin](#module-info-plugin) added.
* Property `mainClass` added to the [projectMetaData](#the-projectmetadata-extension) extension.
* Method [addMainClassAttribute](#main-class-attribute) added to the `manifest` of all `Jar` tasks.
* The `scope` property of the `JavadocMethod` check in the built-in Checkstyle configuration
changed from _private_ to  _package_.
* Scent default version upgraded to 1.0.
* Fixed incorrect group of the quill artifact in the dependency examples in this document.

### version 1.4

* The Maven Import plugin adds methods to the project that sets the group and version from a pom
  file.
* [Core Plugin](#general-usage) added.
* PMD and CPD default versions upgraded to 5.5.7.

### version 1.3

* [Maven Import Plugin](#maven-import-plugin) added.
* The Reports Dashboard plugin creates links to the dashboard reports of any child projects.
* PMD and CPD default versions upgraded to 5.5.2.

### version 1.2

* Quill requires at least Java 1.7 to run.
* [Scent Plugin](#scent-plugin) added. It replaces the JavaNCSS plugin as the Quill standard plugin
for source code metrics since JavaNCSS does not support Java 8 code.
* The JavaNCSS plugin is no longer applied by the 'all' plugin, it must be applied explicitly.
* The Reports Dashboard no longer contains a section for JavaNCSS by default. If the JavaNCSS plugin
is applied its Reports Dashboard section must be added explicitly.
* The JDepend plugin is enhanced to use the guru-nidi fork for Java 8 support.

### version 1.1

* [Pom plugin](#pom-plugin) added.
* The total number of types in the JavaNCSS HTML report now include inner types and local classes,
  and the total number of methods include methods from inner types.
* The JDepend HTML report correctly displays the number of cycles, previously this value always was 0.
* The JDepend summary in the Reports Dashboard has the warning background colour if cycles have been
  detected.
* The Cobertura summary in the Reports Dashboard labels the number of types as 'types', not 'files'.
* Checkstyle default version upgraded to 6.19.
* PMD and CPD default versions upgraded to 5.5.1.

### version 1.0

* Check `JavaDocAuthor` removed from the built-in Checkstyle configuration.
* Checkstyle default version upgraded to 6.17.

### version 0.11

* Cobertura report tasks no longer fail if the input data file doesn't exist (e.g. because no tests
  were executed).
* Loading a built-in configuration no longer fails when using the Gradle daemon.
* Property `ignoreFailures` added to the `javancss` task.
* Checkstyle default version upgraded to 6.16.1.
* The built-in Checkstyle configuration requires version 6.15 or later of Checkstyle.
* Checks based on `RegexpSingleline` in the built-in Checkstyle configuration have unique IDs to
  allow suppressions based on ID.
* Check `IllegalCatch` in the built-in Checkstyle configuration no longer rejects
 `java.lang.Throwable`.
* Rules `AvoidCatchingThrowable` and `OptimizableToArrayCall` removed from the built-in PMD
  configuration.

### version 0.10

* Property `group` added to the `projectMetaData` extension.
* Method `disableTestChecks` added to all enhancements of `CodeQualityExtension` subclasses.
* Tokens `GENERIC_START` and `GENERIC_END` removed from the `NoWhitespaceBefore` check in
  the built-in Checkstyle configuration. Whitespace checking of type parameters is handled by the
  `GenericWhitespace` check.
* Check `InnerAssignment` removed from the built-in Checkstyle configuration.
* Rules `AssignmentInOperand` and `UselessParentheses` removed from the built-in PMD configuration.

### version 0.9

* Initial release.


## General Usage

To use the Quill plugins they must be added to the Gradle build script classpath:

    buildscript {
      ...
      dependencies {
        classpath 'org.myire:quill:1.5'
      ...

The Quill plugins can then be applied to the Gradle project:

    apply plugin: 'org.myire.quill.cpd'
    apply plugin: 'org.myire.quill.scent'

To make all Quill plugins available in a build script, the plugin 'all' can be applied. This plugin
will simply apply all Quill plugins to the Gradle project, thus removing the need to apply the
plugins individually:

    apply plugin: 'org.myire.quill.all'

Note that the 'all' plugin applies both the Ivy Module plugin and the Maven Import plugin. The use
cases where a project uses both Ivy and Maven are probably rare, and most of the time neither of
them is probably needed. For this majority of the use cases the 'core' plugin is a better choice. It
applies all plugins except for the Ivy Module plugin and the Maven Import plugin:

    apply plugin: 'org.myire.quill.core'

### XSL transformation reports

Some of the tasks created or enhanced by the plugins produce HTML reports by applying an XSL
transformation to an XML report created by the same task. This HTML report is a `SingleFileReport`
and is by default created in the same directory as the XML report it is created from. It will have
the same base name as the XML report. For instance, an HTML report created from the XML report
`main.xml` will have the name `main.html`.

In addition to the `SingleFileReport` properties, the XSL style sheet to use can be specified
through the `xslFile` property. This value is a `File` that is resolved relative to the project
directory. If no XSL file is specified the tasks will use a default style sheet bundled with the
Quill jar file.


## Ivy Import Plugin

The Ivy Import plugin imports dependency and/or configuration definitions from an Apache Ivy module
file.  The dependencies and configurations can either by dynamically added to the Gradle project for
the current execution only, or be written to file for later inclusion.

### Usage

    apply plugin: 'org.myire.quill.ivy'

### Project extension

The plugin adds an extension with the name `ivyImport` to the Gradle project. This extension allows
configuring the Ivy module import through a number of properties:

#### Ivy settings file
                                                                              
The Ivy settings file to use is specified in the `settingsFile` property:

    ivyImport.settingsFile = '/path/to/ivy-settings.xml'

The path to the settings file is resolved relative to the project directory. If no settings file is
specified, the default Ivy settings will be used. If a settings file is specified but does not exist
or is inaccessible in some other way, the plugin will fall back to the default Ivy settings.

#### Ivy version

The extension also allows the version of Ivy used for the import to be specified in the property
`ivyVersion`. By default version 2.4.0 is used.

Example:

    ivyImport.ivyVersion = '2.5.0-rc1'

#### Ivy class path

If more fine-grained control over the Ivy classes used is needed than just specifying the Ivy
version, the extension property `ivyClassPath` can be set to a `FileCollection` containing the
desired Ivy classes.

Example:

    ivyImport.ivyClassPath = files('/path/to/customIvy.jar')


### Dynamically importing configurations and dependencies

Configurations and dependencies can be imported and dynamically added to the Gradle project through
the dynamic methods `fromIvyFile` that the plugin adds to the Gradle project's
`ConfigurationContainer` and `DependencyHandler`. These methods take the Ivy module file to import
from as the only argument. The path to the Ivy module file is resolved relative to the project
directory. If no file is passed as argument to `fromIvyFile`, a file called `ivy.xml` in the project
directory will be used.

Specifying

    configurations.fromIvyFile('/path/to/ivy.xml')

in the build script will dynamically add the configurations defined in the specified Ivy module file
to the project.

Specifying

    dependencies.fromIvyFile()

in the build script will dynamically add the dependencies defined in the file `ivy.xml` in the
project directory to the Gradle project.

If a configuration or dependency imported from the Ivy module file already is defined in the Gradle
build script, it will not be overwritten. 

Only dependencies with a configuration that exists in the Gradle project will be added, all others
will be ignored. This means that if only dependencies are imported from an Ivy module file, and some
of the dependencies in that file specify a configuration that is not defined elsewhere, those
dependencies will **not** be added to the project.

Note that importing configurations and dependencies from an Ivy module file is additive. It is thus
possible to mix configurations and dependencies from an Ivy module file with explicitly defined
ones, and to import from multiple Ivy files:

    configurations {
      myExplicitConfiguration
      fromIvyFile()
    }

    dependencies {
        fromIvyFile('/another/ivy.xml')
        testCompile "org.mockito:mockito-core:1.10.19"
    }

### Dynamically setting the group and version

An Ivy module file's `organisation` and `revision` can be imported and applied to the Gradle project
through the dynamic methods `setGroupFromIvyFile` and `setVersionFromIvyFile` that the plugin adds
to the Gradle project. These methods take the Ivy file to import from as their only argument. The
path to the Ivy file is resolved relative to the project directory.

Specifying

    setGroupFromIvyFile('/path/to/ivy.xml')

will set the project's `group` property to the value of the `organisation` attribute in the Ivy
file's `info` element.

Similarly, the dynamic method `setVersionFromIvyFile` will set the  project's `version` property to
the value of the `revision` attribute in the Ivy file's `info` element.

If no file is specified, the default value 'ivy.xml' is assumed. Thus, specifying

    setGroupFromIvyFile()

will set the project's `group` property to the value of the `organisation` element in a file called
'ivy.xml' in the Gradle project directory.

### Converting configurations and dependencies

Configurations and dependencies can be read from an Ivy module file and written to a Gradle file
with the `convertIvyModule` task. This task imports and converts Ivy configurations and dependencies
in the same way as the dynamic methods `fromIvyFile` described above, but instead of adding the
configurations and dependencies to the Gradle project the task writes them to a file. This file can
then be applied to the Gradle build script in future executions.

The `convertIvyModule` task's behaviour can be configured through the following properties:

* `ivyFile` - the path to the Ivy module file to import from. The path is resolved relative to the
project directory. Default is 'ivy.xml', i.e. a file called 'ivy.xml' in the Gradle project
directory.

* `destination` - the path to the file to write the converted configurations and dependencies to.
The path is resolved relative to the project directory. Default is a file called
'dependencies.gradle' in the same directory as the Ivy file.

* `overwrite` - a boolean specifying whether or not to replace the destination file if it exists. If
this property is `false` and the file specified in the `destination` property exists, the task will
do nothing. Default is `true`.

* `convertConfigurations` - a boolean specifying whether or not to import and convert configurations
from the Ivy file. Default is `true`.

* `convertDependencies` - a boolean specifying whether or not to import and convert dependencies
from the Ivy file. Default is `true`.

The task will use the Ivy settings file specified in the `ivyImport` project extension, or the
default Ivy settings if no explicit settings file has been specified.


## Maven Import Plugin

The Maven Import plugin imports repository and/or dependency definitions from a Maven pom file. The
repositories and dependencies can either by dynamically added to the Gradle project for the current
execution only, or be written to file for later inclusion.

The plugin does **not** convert the entire pom file to a Gradle build script like the standard
Gradle Build Init plugin does. It only operates on the repositories and dependencies in the pom
file.

### Usage

    apply plugin: 'org.myire.quill.maven'

### Dependency configuration

The Maven Import plugin adds a dependency configuration with the name `mavenImport` to the project.
This configuration specifies the default classpath for the Maven classes used by the plugin, and it
contains dependencies equivalent to:

    mavenImport 'org.apache.maven:maven-embedder:<mavenVersion>'
    mavenImport 'org.slf4j:slf4j-nop:1.7.26'

where `<mavenVersion>` is the value of the `mavenImport` extension's property with the corresponding
name, see below.

### Project extension

The plugin adds an extension with the name `mavenImport` to the Gradle project. This extension
allows configuring the pom file import through a number of properties:

#### Maven settings file

The Maven settings file to use is specified in the `settingsFile` property:

    mavenImport.settingsFile = '/path/to/settings.xml'

The path to the settings file is resolved relative to the project directory. If no settings file is
specified, the default Maven settings will be used, as specified in the
[Maven documentation](https://maven.apache.org/settings.html#Quick_Overview).

#### Scope mapping

The extension's property `scopeToConfiguration` contains the mapping from Maven dependency scope to
Gradle configuration used by the dependency import. This property is a map from scope name to
configuration name. A scope with an entry in this mapping will be mapped to the configuration with
the name corresponding to the entry's value. A scope with no mapping will be mapped to the
configuration with the same name as the scope. Scopes mapped explicitly to null will be ignored by
the dependency import.

By default this property contains the mappings

* 'test' -> 'testCompile'
* 'provided' -> 'compileOnly'

meaning that the scopes 'compile', 'runtime', and 'system' will be mapped to configurations with the
same names.

Example: to ignore all dependencies with scope 'system' and to map the 'provided' scope to the
'compile' configuration instead of 'compileOnly', the mapping should be configured as

    mavenImport.scopeToConfiguration['system'] = null
    mavenImport.scopeToConfiguration['provided'] = 'compile'

#### Maven version

The extension also allows the version of Maven used by the plugin to be specified in the property
`mavenVersion`. By default version 3.6.1 is used.

Example:

    mavenImport.mavenVersion = '3.3.9'

#### Maven class path

If more fine-grained control over the Maven classes used is needed, the extension property
`mavenClassPath` can be set to a `FileCollection` containing the desired Maven classes. One way to
do this is through a dependency configuration:

    configurations { customMavenImport }
    dependencies {
        customMavenImport 'org.apache.maven:maven-embedder:3.3.3'
        customMavenImport 'org.slf4j:slf4j-simple:1.7.25'
    }
    mavenImport.mavenClassPath = configurations['customMavenImport']

### Dynamically importing repositories and dependencies

Repositories and dependencies can be imported and dynamically added to the Gradle project through
the dynamic method `fromPomFile` that the plugin adds to the Gradle project's `RepositoryHandler`
and `DependencyHandler`. This method takes the pom file to import from as its only argument. The
path to the pom file is resolved relative to the project directory.

Specifying

    dependencies.fromPomFile('/path/to/pom.xml')

in the build script will dynamically add the dependencies defined in the effective pom of the file
passed to the method.

If no file is specified, the default value 'pom.xml' is assumed. Thus, specifying

    repositories.fromPomFile()

will import repositories from a file called 'pom.xml' in the Gradle project directory.

Importing repositories and dependencies from a Maven pom file is additive. Any repositories and
dependencies defined elsewhere in the Gradle build script will not be overwritten. It is thus
possible to mix repositories and dependencies from a pom file with explicitly defined ones:

    repositories {
        fromPomFile('/path/to/pom.xml')
        flatDir { dirs "${project.projectDir}/lib" }
    }
    dependencies {
        fromPomFile()
        testRuntime 'com.h2database:h2:1.4.199'
    }

In the example above the repositories are imported from an explicit pom file and the dependencies
from the default pom file. It is also possible to import repositories or dependencies from several
pom files.

Note that importing repositories creates a chicken-and-egg situation. In order to import a pom file
the plugin needs the external libraries from the `mavenImport` dependency configuration (unless
another class path has been specified in the extension property `mavenClassPath`, see above), and
these libraries must be retrieved from a repository. If this repository is defined in the pom file,
it will not be available when resolving the dependency configuration.

To handle this situation, the plugin uses a temporary repository if no repositories are available
when the `mavenImport` configuration is about to be resolved. This temporary repository is specified
in the `mavenImport` extension's property `classpathRepository`, and defaults to the repository
returned by `project.repositories.mavenCentral()`.

The property should be set to an `ArtifactRepository` or to a closure that returns an instance of
that type. Setting this property to null will disable using a temporary repository when resolving
the configuration's dependencies.

Example:

    mavenImport.classpathRepository = { project.jcenter() }

### Dynamically setting the group and version

A pom file's `groupId` and `version` can be imported and applied to the Gradle project through the
dynamic methods `applyGroupFromPomFile` and `applyVersionFromPomFile` that the plugin adds to the
Gradle project. These methods take the pom file to import from as their only argument. The path to
the pom file is resolved relative to the project directory.

Specifying

    applyGroupFromPomFile('/path/to/pom.xml')

will set the project's `group` property to the value of the `groupId` element in the effective pom
of the file passed to the method.

If no file is specified, the default value 'pom.xml' is assumed. Thus, specifying

    applyVersionFromPomFile()

will set the project's `version` property to the value of the `version` element in a file called
'pom.xml'  in the Gradle project directory.

### Converting repositories and dependencies

Repositories and dependencies can be read from a pom file and written to a Gradle file with the
`convertPom` task. This task imports and converts Maven repositories and dependencies in the same
way as the dynamic methods `fromPomFile` described above, but instead of adding the repositories and
dependencies to the Gradle project the task writes them to a file. This file can then be applied to
the Gradle build script in future executions.

The `convertPom` task's behaviour can be configured through the following properties:

* `pomFile` - the path to the Maven pom file to import from. The path is resolved relative to the
project directory. Default is 'pom.xml', i.e. a file called 'pom.xml' in the Gradle project
directory.

* `destination` - the path to the file to write the converted repositories and dependencies to. The
path is resolved relative to the project directory. Default is a file called 'dependencies.gradle'
in the same directory as the pom file.

* `overwrite` - a boolean specifying whether or not to replace the destination file if it exists. If
this property is `false` and the file specified in the `destination` property exists, the task will
do nothing. Default is `true`.

* `convertRepositories` - a boolean specifying whether or not to import and convert repositories
from the pom file. Default is `true`.

* `convertDependencies` - a boolean specifying whether or not to import and convert dependencies
from the pom file. Default is `true`.

The task will use the Maven settings file specified in the `mavenImport` project extension, or the
default Maven settings if no explicit settings file has been specified.


## Project Metadata Plugin

The Project Metadata plugin adds two extensions to the Gradle project, `projectMetaData` and
`semanticVersion`. These two extensions allow additional metadata and version properties to be
specified for the project.

### Usage

    apply plugin: 'org.myire.quill.meta'

### Common features

The properties for both extensions can either be set explicitly or be loaded from a JSON file. In
the latter case, the file is loaded by passing the path to the file to the extension's `from`
method:

    projectMetaData.from '/path/to/project-meta.json'
    semanticVersion.from '/path/to/version.json'

The path to these JSON files are resolved relative to the project directory. It is possible to
mix specifying the properties explicitly in the build scripts and loading them from file. If a
property is set both from the JSON file and explicitly in the build script, the value that is set
last in the build script takes precedence.

### The projectMetaData extension

This extension holds project meta data properties:

* `shortName` - A string with the project's short name, for example "Quill".

* `longName` - A string with the project's long name, for example "Quill Gradle Plugins".

* `group` - A string with the project's group name, for example "org.myire".

* `description` - A string with the project's description, for example "Additional tasks and
opinionated defaults for Gradle build scripts".

* `mainPackage` - A string with the main package name on a format suitable for a `java.lang.Package`
specification in a manifest file, for example "org/myire/quill/".

* `mainClass` - A string with the fully qualified name of the project's main class, if applicable.

Setting the properties explicitly in the build script is straight-forward:

    projectMetaData {
        shortName = 'Quill'
        longName = 'Quill Gradle plugins'
        group = 'org.myire'
        description = 'Additional tasks and opinionated defaults for Gradle build scripts'
        mainPackage = 'org/myire/quill/'
        mainClass = 'org.myire.quill.Main'
    }

If the properties are loaded from a JSON file, that file should contain a single object with the
extension properties:

    {
      "shortName": "Quill",
      "longName": "Quill Gradle plugins",
      "group": "org.myire",
      "description": "Additional tasks and opinionated defaults for Gradle build scripts",
      "mainPackage" : "org/myire/quill/",
      "mainClass" : "org.myire.quill.Main"
    }

### The semanticVersion extension

This project extension holds project version info based on semantic versioning, see
[http://semver.org](http://semver.org). The extension has the following properties:

* `major` - An integer holding the major component of the semantic version number.

* `minor` - An integer holding the minor component of the semantic version number.

* `patch` - An integer holding the patch component of the semantic version number.

* `preReleaseIdentifiers` - An array with zero or more pre-release identifier strings. A null array
is interpreted as an empty array.

* `buildMetaData` - An array with zero or more build meta data strings. A null array is interpreted
as an empty array.

These properties can be set explicitly in the build script:

    semanticVersion {
        major = 2
        minor = 0
        patch = 1
        preReleaseIdentifiers = ['alpha,'5']
        buildMetaData = '20150115T100027'
    }

If loaded from a JSON file, that file should contain a single object with the extension properties:

    {
      "major": 2,
      "minor": 0,
      "patch": 1,
      "preReleaseIdentifiers": ["alpha","5"],
      "buildMetaData": ["20150115T100027"]
    }


## Java Additions Plugin

The Java Additions plugin configures existing Java tasks with default values, enhances existing Java
tasks, and adds new Java related tasks.

### Usage

    apply plugin: 'org.myire.quill.java'

The Java Additions plugin makes sure the built-in Java plugin has been applied to the project,
meaning it isn't necessary to apply that plugin explicitly.

### Default values

The plugin changes the default values of some Java related tasks.

All tasks that have the type `JavaCompile` (by default `compileJava` and `compileTestJava`) are
configured to have the compile option `deprecated` set to true and the compile option `encoding` set
to 'UTF-8'. This is equivalent to having the following in the build script:

    tasks.withType(JavaCompile) {
      options.deprecation = true
      options.encoding = 'UTF-8'
    }

The `test` task is configured to ignore failures, this is equivalent to

    test {
      ignoreFailures = true
    }

The `javadoc` task is configured to include protected and public members, to include author and
version tags, and to create class and package usage pages. This is equivalent to having the
following in the build script:

    javadoc {
      options.showFromProtected()
      options.author = true
      options.version = true
      options.use = true
    }

### Additional tasks

The Java Additions plugin adds two tasks of type `Jar` to the project.

The `sourcesJar` task assembles a jar archive containing the main source set's source code, i.e. the
sources specified by the project property `sourceSets.main.allSource`. The task's `classifier` is
set to `sources`, meaning that the default name of the jar will be `<basename>-sources.jar`.


The `javadocJar` task assembles a jar archive containing the main JavaDocs, i.e. the directory
specified in the `javadoc` task's `destinationDir` property. Consequently, the `javadocJar` depends
on the `javadoc` task. The task's `classifier` is set to `javadoc`, meaning that the default name of
the jar will be `<basename>-javadoc.jar`.

Both tasks are added to the `archives` project artifact.

### Manifest enhancements

The plugin adds four methods to the `manifest` of all tasks that have the type `Jar`.

#### Main-Class attribute

The `addMainClassAttribute()` method will add a 'Main-Class' attribute to the manifest. The value
for this attribute is taken from the parameter passed to the method. If no parameter is passed, the
value of the property `projectMetaData.mainClass` is used. If none of these two values are
available, the 'Main-Class' attribute will **not** be added  to the manifest.

The following configuration of the `jar` task will put the value 'org.myire.sample.Main' into the
manifest's 'Main-Class' attribute:

    jar {
      manifest {
        addMainClassAttribute 'org.myire.sample.Main'
        ...
      }
      ...
    }

The manifest will then look like:

    Manifest-Version: 1.0
    Main-Class: org.myire.sample.Main
    ...

#### Class-Path attribute

The `addClassPathAttribute()` method will add a 'Class-Path' attribute to the manifest. This
classpath will be constructed from the source set(s) passed as parameters to the method. All jar
files from the `runtimeClasspath` of each `SourceSet` will be put into the 'Class-Path' attribute.

The following configuration of the `jar` task will get all jar files from the main source set's
runtime classpath and put them into the manifest's 'Class-Path' attribute:

    jar {
      manifest {
        addClassPathAttribute(sourceSets.main)
        ...
      }
      ...
    }

The manifest will then look like:

    Manifest-Version: 1.0
    Class-Path: lib1.jar lib2.jar lib3.jar
    ...

#### Build-Info section

The `addBuildInfoSection()` method will add a section with the name 'Build-Info' to the manifest.
This section will have attributes specifying the current time when the jar file was built, and the
Java version, JVM, and OS used to build the jar file.

To add the 'Build-Info' section, simply add the method in the manifest's configuration:

    jar.manifest {
      addBuildInfoSection()
      ...
    }

The 'Build-Info' section in the manifest will look like this:

    Name: Build-Info
    Build-Time: 20150922T110830.614+0200
    Build-JRE: 1.8.0_60-b27
    Build-VM: Java HotSpot(TM) 64-Bit Server VM 25.60-b23
    Build-OS: Mac OS X 10.9.5

#### Package version section

The `addPackageSection()` method will add a package version section to the manifest. The attributes
in this section will be the title and version attributes specified in the
[documentation](http://docs.oracle.com/javase/tutorial/deployment/jar/packageman.html) for package
versioning in the manifest.

The package section in the manifest will look something like this:

    Name: name/of/package/
    Specification-Title: Short Title
    Specification-Version: 3.0
    Implementation-Title: Long Title
    Implementation-Version: 3.0-alpha.2+20150106T174539

The plugin first tries to get the values for the package attributes from the `projectMetaData` and
`semanticVersion` extensions that are added by the
[Project Metadata Plugin](#project-metadata-plugin). If those extensions are not available, the
values are retrieved from standard Gradle project properties.

* The package name is taken from the parameter passed to the method. If no parameter was passed, the
property `projectMetaData.mainPackage` is used. If none of these two values are available, the
package section will **not** be added  to the manifest.

* The 'Specification-Title' value is taken from `projectMetaData.shortName` or, if that property
does not exist or is null, from the standard property `project.name`.

* The 'Specification-Version' value is taken from `semanticVersion.shortVersionString` or, if that
property does not exist or is null, from the standard property `project.version`.

* The 'Implementation-Title' value is taken from `projectMetaData.longName` or, if that property
does not exist or is null, from the standard property `project.name`.

* The 'Implementation-Version' value is taken from `semanticVersion.longVersionString` or, if that
property does not exist or is null, from the standard property `project.version`.

To add a package version section with the name taken from `projectMetaData.mainPackage` the manifest
should be configured like this:

    jar {
      manifest {
        addPackageSection()
        ...
      }
      ...
    }

To add a package version section with a specific name the manifest should be configured like this:

    jar {
      manifest {
        addPackageSection('name/of/package')
        ...
      }
      ...
    }

Since these methods are added to the manifest of all tasks of type `Jar`, it means that the added
tasks `sourcesJar` and `javadocJar` can have e.g. a 'Build-Info' section in their manifests:

    sourcesJar.manifest.addBuildInfoSection()

and

    javadocJar.manifest.addBuildInfoSection()


## JUnit Additions Plugin

The JUnit Additions plugin adds a JUnit summary report to the `Test` task. If the `Test` task isn't
present in the project, the plugin has no effect.

### Usage

    apply plugin: 'org.myire.quill.junit'


### JUnit summary report

The plugin adds a `junitSummaryReport` property to the `Test` task's convention. This is a
`SingleFileReport` that aggregates individual JUnit XML report files into a summary XML report.

The report is `enabled` by default and is created last when the `Test` task is executed. In addition
to the `SingleFileReport` properties, the following are supported:

* `junitReportDirectory` - a `File` specifying the directory with the JUnit XML reports to
aggregate. The default is the directory specified by the `Test` task's property
`reports.junitXml.destination`.

* `fileNamePattern` - A string with a regular expression that the names of the JUnit report files to
aggregate must match. Only the files in the `junitReportDirectory` that match this pattern will be
aggregated into the summary report. The default pattern is "^TEST\\-.*\\.xml$".

The summary report's `destination` is by default a file called `junitSummary.xml` in the `junit`
subdirectory of the project report directory. This destination can be modified as with any Gradle
report:

    test {
        summaryReport.destination = "$buildDir/aggregates/better_report_name.xml"
    }

When producing the summary report, the plugin will parse each XML file in the `junitReportDirectory`
that matches `fileNamePattern`. The parsing will extract the attributes listed below that exist in
the XML root element and add their values to the aggregation:

* `tests` - the number of tests that were run
* `skipped` - the number of tests that were skipped
* `failures` - the number of tests that failed
* `errors` - the number of tests that had runtime errors
* `timestamp` - the date and time when the tests were run
* `time` - the time elapsed when running the tests

This matches the normal JUnit XML report root element which looks something like:

    <testsuite tests="8"
               skipped="1"
               failures="2"
               errors="0"
               timestamp="2015-01-19T08:12:20"
               time="0.061">
    ....
    </testsuite>

where some attributes not used for the summary report have been omitted for brevity.

The summary report will consist of one XML element:

    <junit-summary testsuites="3"
                   tests="17"
                   skipped="0"
                   failures="1"
                   errors="0"
                   total-time="0.086"
                   start-date="2015-09-28"
                   start-time="10:12:20"
                   end-date="2015-09-28"
                   end-time="10:12:20"/>

where the attributes have the following meaning:

* `testsuites` - the number of XML files aggregated into the summary
* `tests` - the sum of all `tests` attribute values in the processed XML files
* `skipped` - the sum of all `skipped` attribute values in the processed XML files
* `failures` - the sum of all `failures` attribute values in the processed XML files
* `errors` - the sum of all `errors` attribute values in the processed XML files
* `total-time` - the sum of all `time` attribute values in the processed XML files
* `start-date` - the date part of the lowest `timestamp` attribute value found in the processed XML
files
* `start-time` - the time part of the lowest `timestamp` attribute value found in the processed XML
files
* `end-date` - the date part of the highest `timestamp` attribute value found in the processed XML
files
* `end-time` - the time part of the highest `timestamp` attribute value found in the processed XML
files


## JaCoCo Additions Plugin

The JaCoCo Additions plugin applies the standard Gradle plugin `jacoco` to the project and
configures the corresponding project extension and tasks with some defaults. The plugin applies the
standard `java` plugin before applying the `jacoco` plugin to make the latter add the
`jacocoTestReport` task to the project.

### Usage

    apply plugin: 'org.myire.quill.jacoco'

### Default values

The plugin configures the `jacoco` extension in the project to use version 0.8.4 of JaCoCo. This is
equivalent to configuring the extension explicitly in the build script as follows:

    jacoco {
      toolVersion = '0.8.4'
    }

The plugin also configures the `jacoco` extension added to all test tasks by setting the `append`
property to false. This is however only done if running a Gradle version less than 5.0, since the
`append` property was deprecated in Gradle version 5.0. This is equivalent to

    test {
      jacoco {
        append = false
      }
    }

The `jacocoTestReport` task is configured to have the `xml` and `html` reports enabled and the `csv`
report disabled. The `build`  task is also set to depend on the `jacocoTestReport` task.


## SpotBugs Additions Plugin

The SpotBugs Additions plugin configures the project extension and tasks added by the
[SpotBugs Gradle plugin](https://plugins.gradle.org/plugin/com.github.spotbugs) with some defaults
and additions.

Note that unlike the other plugins that enhance existing plugins, the SpotBugs Additions plugin does
_not_ apply the SpotBugs Gradle plugin. That plugin must be applied explicitly in the build script.

### Usage

    plugins { id 'com.github.spotbugs' version '2.0.0' }
    apply plugin: 'org.myire.quill.spotbugs'

### Default values

The plugin configures the `spotbugs` extension in the project to let the build continue even if
violations are found, and to use SpotBugs version 3.1.12 as the default version. This is equivalent
to configuring the extension explicitly in the build script as follows:

    spotbugs {
      ignoreFailures = true
      toolVersion = '3.1.12'
    }

All tasks of type `SpotBugsTask` (normally `spotbugsMain` and `spotbugsTest`) are configured to
produce XML reports with messages, which is equivalent to the build script configuration

    tasks.withType(SpotBugsTask) {
      reports.xml.withMessages = true
    }

### Extension additions

A method with the name `disableTestChecks` is added to the `spotbugs` extension. If this method is
called in the build script it will remove the `test` source set from the extension's source sets,
thus disabling the `spotbugsTest` task:

    spotbugs.disableTestChecks()

### Task additions

The plugin adds an [XSL transformation report](#xsl-transformation-reports) to the `SpotBugsTask` 
tasks. These reports have the name `quillHtmlReport` and are `enabled` by default. They can be
configured per task, e.g. to use another XSL file than the one distributed in the Quill jar:

    spotbugsMain {
      quillHtmlReport.xslFile = 'xsl/spotbugs.xsl'
      quillHtmlReport.destination = "$buildDir/reports/spotbugs.html"
    }

    spotbugsTest.quillHtmlReport.enabled = false


## Checkstyle Additions Plugin

The Checkstyle Additions plugin applies the standard Gradle plugin `checkstyle` to the project and
configures the corresponding project extension and tasks with some defaults and additions.

### Usage

    apply plugin: 'org.myire.quill.checkstyle'

### Default values

The plugin configures the `checkstyle` extension in the project to let the build continue even if
violations are found, and to not log every found violation. The Checkstyle version to use is set to
8.23. This is equivalent to configuring the extension explicitly in the build script as follows:

    checkstyle {
      ignoreFailures = true
      showViolations = false
      toolVersion = '8.23'
    }

The Checkstyle configuration file is specified to be the one bundled with the Quill jar. This
configuration file is extracted to the path "tmp/checkstyle/checkstyle_config.xml" relative to the
project's build directory. Note that this configuration file requires at least version 8.22 of
Checkstyle. When setting `toolVersion` to an older version of Checkstyle, another configuration file
must be explicitly configured.

This built-in Checkstyle configuration file can optionally use a suppression filter file. The
location of this file is specified through the Checkstyle configuration property
`suppressions.file`, which can be set in the `checkstyle` extension:

    checkstyle.configProperties.put('suppressions.file', file('checkstyle_suppressions.xml'))

If this property isn't specified, no suppression filter file is used.

### Extension additions

The plugin adds a method with the name `disableTestChecks` to the `checkstyle` extension. If this
method is called in the build script it will remove the `test` source set from the extension's
source sets, thus disabling the `checkstyleTest` task:

    checkstyle.disableTestChecks()

### Task additions

The plugin adds an [XSL transformation report](#xsl-transformation-reports) to all tasks of type
`Checkstyle`. These reports have the name `quillHtmlReport` and are `enabled` by default. They can
be configured per task, e.g. to use another XSL file than the one distributed in the Quill jar:

    checkstyleMain {
      quillHtmlReport.xslFile = 'resources/checkstyle.xsl'
      quillHtmlReport.destination = "$buildDir/reports/checkstyle/checkstyle.html"
    }

    checkstyleTest.quillHtmlReport.enabled = false

The plugin disables the standard HTML report for all tasks of type `Checkstyle`
(normally `checkstyleMain` and `checkstyleTest`). This is equivalent to the build script

    tasks.withType(Checkstyle) {
      reports.html.enabled = false
    }

The standard HTML report can of course be enabled alongside with the XSL transformation report. Note
however that these two reports by default have the same file destination, meaning that one will
overwrite the other. If both are to be enabled at least one of them should be configured to have a
non-default file destination, e.g.

    checkstyleMain.reports.html.enabled = true
    checkstyleMain.quillHtmlReport.destination = "$buildDir/reports/checkstyle/quill.html"


## PMD Additions Plugin

The PMD Additions plugin applies the standard Gradle plugin `pmd` to the project and configures the
corresponding project extension and tasks with some defaults and additions.

### Usage

    apply plugin: 'org.myire.quill.pmd'

### Default values

The plugin configures the `pmd` extension in the project to let the build continue even if
violations are found, and to use version 6.17.0 of PMD. This is equivalent to configuring the
extension explicitly in the build script as follows:

    pmd {
      ignoreFailures = true
      toolVersion = '6.17.0'
    }

The plugin removes the built-in PMD rule sets from the extension's configuration and specifies that
the rule set file bundled with the Quill jar is used. This file is extracted to the path
"tmp/pmd/pmd_rules.xml" relative to the project's build directory.

The built-in rule set file is specified through the extension's `ruleSetFiles` property. If the
extension is configured to use another rule set file through the incubating `ruleSetConfig`
property, the built-in rule set file will still be in use. It must be explicitly disabled by setting
`ruleSetFiles` to en empty file collection, e.g. `pmd.ruleSetFiles = files()`.

Note that the built-in rule set file requires at least version 6.12.0 of PMD. When setting
`toolVersion` to an older version of PMD, another rule set file must be explicitly configured.

### Extension additions

A method with the name `disableTestChecks` is added to the `pmd` extension. Calling this method in
the build script it will remove the `test` source set from the extension's source sets, thus
disabling the `pmdTest` task:

    pmd.disableTestChecks()

### Task additions

The plugin adds an [XSL transformation report](#xsl-transformation-reports) to all tasks of type
`PMD`. These reports have the name `quillHtmlReport` and are `enabled` by default. They can be
configured per task, e.g. to use another XSL file than the one distributed in the Quill jar:

    pmdMain {
      quillHtmlReport.xslFile = 'resources/pmd.xsl'
      quillHtmlReport.destination = "$buildDir/reports/static_analysis/pmd.html"
    }

    pmdTest.quillHtmlReport.enabled = false

Note that the default XSL file requires XML reports produced by PMD 6.0 or later.

The plugin disables the standard HTML report for all tasks of type `Pmd` (normally `pmdMain` and
`pmdTest`). This is equivalent to the build script

    tasks.withType(Pmd) {
      reports.html.enabled = false
    }

The standard HTML report can of course be enabled alongside with the XSL transformation report. Note
however that these two reports by default have the same file destination, meaning that one will
overwrite the other. If both are to be enabled at least one of them should be configured to have a
non-default file destination, e.g.

    pmdMain.reports.html.enabled = true
    pmdMain.quillHtmlReport.destination = "$buildDir/reports/pmd/quill.html"

The plugin also adds a read-only `filter` property to all tasks of type `PMD`. This property allows
an XML file containing filters for PMD rule violations to be specified. These filters are applied
last in the task to all violations found by PMD. The violations that are matched by the filters will
be removed from the task's XML report.

The file is specified relative to the project directory:

    pmdMain {
      filter.file = 'pmd_filters.xml'
    }

It is possible to disable the filter function per task:

    pmdTest.filter.enabled = false

The filter file defines a collection of rule violation filters on the format

    <?xml version="1.0"?>
    <rule-violation-filters>
      <rule-violation-filter .../>
      ...
    </rule-violation-filters>

A rule violation filter element has one mandatory attribute, *files*, that contains a regular
expression specifying the file name(s) that the filter applies to.

The optional *rules* attribute contains a regular expression specifying the names of the PMD rules
that the filter applies to. If this attribute isn't specified, all rules are filtered out in the
(parts of) the file(s) specified.

The *lines* attribute contains a comma-separated list of line numbers and line number ranges that
specify the line(s) of the file(s) that the filter should be applied to. If this attribute isn't
specified, the filter applies to the entire file(s).

Examples:

Filter out rule "r1" in file "AAA.java":

    <rule-violation-filter files="AAA.java" rules="r1"/>

Filter out rule "r1" in file "XXX.java" at lines 2, 5 and 130-156:

    <rule-violation-filter files="XXX.java" lines="2,5,130-156" rules="r1"/>

Filter out all rules between lines 17 and 26 in file "Dummy.java":

    <rule-violation-filter files="Dummy.java" lines="17-26"/>

Filter out rules "r1" and "r3" in files matching the pattern ".\*Parser.\*\\.java":

    <rule-violation-filter files=".*Parser.*\.java" rules="^(r1|r3)$"/>

Filter out everything in files matching the pattern ".\*Test.\*\\.java":

    <rule-violation-filter files=".*Test.*\.java"/>


## CPD Plugin

The CPD plugin adds a verification task for copy-paste detection using the CPD tool that is part of
the [PMD](https://pmd.github.io/latest) distribution.

The plugin works with CPD version 6.1 or later.

### Usage

    apply plugin: 'org.myire.quill.cpd'

### Task

The plugin adds a `cpd` task to the project and also adds it to the dependencies of the `check`
task. The `cpd` task operates on source files and is a subclass of
`org.gradle.api.tasks.SourceTask`, thereby using the standard mechanisms for specifying which source
files to analyze, such as include and exclude patterns. By default, the `cpd` task uses the `main`
source set's Java files as input files.

In addition to the standard source task properties, the `cpd` task's behaviour can be configured
through the properties described below. Most of these properties are direct equivalents to one of the
[CPD command line options](https://pmd.github.io/latest/pmd_userdocs_cpd.html#cli-options-reference).

* `toolVersion` - a string specifying the version of CPD to use. The default is the version
specified in `pmd.toolVersion`, or, if the `pmd` extension isn't available in the project, version
"6.16.0". Note however that `pmd.toolVersion` is only used if it is equal to or greater than the
minimum CPD version "6.1.0".

* `cpdClasspath` - a `FileCollection` specifying the classpath containing the CPD classes used by
the task. The default is the `cpd` dependency configuration (see below).

* `encoding` - a string specifying the encoding used by CPD to read the source files and to produce
the report. The platform's default encoding will be used if this property isn't specified.

* `language` - a string specifying the language of the source files to analyze, e.g. "cpp", "java",
"php", "ruby", or "ecmascript". See the
[CPD documentation](https://pmd.github.io/latest/pmd_userdocs_cpd.html#supported-languages) for a
list of the languages supported by CPD. The default is "java".

* `minimumTokenCount` - an integer specifying the minimum duplicate size to be reported. Defaults
to 100.

* `ignoreLiterals` - if this boolean is true, CPD ignores literal value differences when evaluating
a duplicate block. This means `foo=42;` and `foo=43;` will be seen as equivalent. Default is false.

* `ignoreIdentifiers` - a boolean similar to `ignoreLiterals`, differences in e.g. variable names
and method names will be ignored. Default is false.

* `ignoreAnnotations` - if this boolean is true, annotations will be ignored. This property can be
useful when analyzing code based one some frameworks where annotations become very repetitive.
Default is false.

* `skipDuplicateFiles` - if this boolean is true, CPD will ignore multiple copies of files with the
same name and length. Default is false.

* `skipLexicalErrors` - if this boolean is true, CPD will skip files which can't be tokenized due to
invalid characters instead of aborting the analysis. Default is false.

* `skipBlocks` - if this boolean is true, blocks with the pattern specified in the
`skipBlocksPattern` property will be excluded from the analysis. Default is false.

* `skipBlocksPattern` - a string specifying the pattern to find the blocks to skip when `skipBlocks`
is true. The value contains two parts, separated by a `|`. The first part is the start pattern, the
second part is the ending pattern. The default value is "#if 0|#endif".

* `ignoreUsings` - if this boolean is true, *using* directives in C# will be ignored in the
analysis. Default is false.

* `reports` - a `ReportContainer` holding the reports created by the task, see below.

### Reports

The `cpd` task creates a primary report with the result of the copy-paste detection. This report can
be on one of the formats supported by CPD, see the
[documentation](https://pmd.github.io/latest/pmd_userdocs_cpd.html#available-report-formats). If the
primary report is on the XML format, the task can optionally produce an HTML report by applying an
XSL transformation on the XML report.

The two reports are configured through the `reports` property of the `cpd` task:

* `primary` - a `SingleFileReport` that also allows the format to be specified in its `format`
property. Valid values for the format are "xml", "text", "csv", "csv_with_linecount_per_file", or
"vs", with "xml" as the default value. The default report is a file called "cpd.*ext*" where *ext*
is the value of the `format` property. The default report file is located in a directory called
"cpd" in the project's report directory or, if no project report directory is defined, in a
directory called "cpd" in the project's build directory.

* `html` - a `SingleFileReport` that will be created if the primary report is `enabled` and its
`format` is "xml". This report produces an HTML version of the tasks's XML report by applying an XSL transformation. By default, the HTML report is created in the same directory as the XML report
and given the same base name as the XML report (e.g. "cpd.html" if the XML report has the name
"cpd.xml"). The XSL style sheet to use can be specified through the `xslFile` property. This
property is a `File` that is resolved relative to the project directory. If no XSL file is specified
the default style sheet bundled with the Quill jar file will be used.

The reports can be configured with a closure, just as any other `ReportContainer`:

    cpd {
      ...
      reports {
        primary.destination = "$buildDir/reports/cpdReport.xml"
        html.xslFile = 'xsl/cpd.xsl'
      }
    }

The reports can also be configured through chained access of the properties:

    cpd {
      ...
      reports.primary.format = 'csv'
    }

### Dependency configuration

The CPD plugin adds a `cpd` dependency configuration to the project. This configuration specifies
the default classpath for the `cpd` task. By default, this configuration has one dependency,
equivalent to:

    cpd 'net.sourceforge.pmd:pmd-dist:<toolVersion>'

where `<toolVersion>` is the value of the `cpd` task's `toolVersion` property.


## Scent Plugin

The Scent plugin adds a task for collecting source code metrics using the
[Scent](https://github.com/handmadecode/scent) library.

### Usage

    apply plugin: 'org.myire.quill.scent'

### Task

The plugin adds a `scent` task to the project and also adds the task to the dependencies of the
`build` task. The `scent` task operates on Java source files and is a subclass of
`org.gradle.api.tasks.SourceTask`, thereby using the standard mechanisms for specifying which source
files to collect metrics for, such as include and exclude patterns. By default, the `scent`
task uses the `main` source set's Java files as input files.

In addition to the standard source task properties, the `scent` task's behaviour can be configured
through the following properties:

* `toolVersion` - a string specifying the version of Scent to use. Default is version "1.0".

* `sourceEncoding` - a string specifying the encoding of the Java files, e.g. "UTF-8". If this
property isn't specified the platform's default encoding will be used.

* `reports` - a `ReportContainer` holding the reports created by the task, see below.

* `scentClasspath` - a `FileCollection` specifying the classpath containing the Scent classes used
by the task. The default is the `scent` dependency configuration, see below.

### Reports

The `scent` task creates an XML report with the collected source code metrics. The task can also
produce an HTML report by applying an XSL transformation on the XML report.

The two reports are configured through the `reports` property of the `scent` task:

* `xml` - a `SingleFileReport` that creates an XML file with the collected source code metrics. The
default is a file called "scent.xml"  located in a directory called "scent" in the project's report
directory or, if no project report directory is defined, in a directory called "scent" in the
project's build directory.

* `html` - a `SingleFileReport` that will be created if the XML report is `enabled`. This report
produces an HTML version of the tasks's XML report by applying an XSL transformation. By default,
the HTML report is created in the same directory as the XML report and given the same base name as
the XML report (e.g. "scent.html" if the XML report has the name "scent.xml"). The XSL style sheet
to use can be specified through the `xslFile` property. This property is a `File` that is resolved
relative to the project directory. If no XSL file is specified the default style sheet bundled with
the Quill jar file will be used.

The reports can be configured with a closure, just as any other `ReportContainer`:

    scent {
      ...
      reports {
        xml.destination = "$buildDir/reports/metrics.xml"
        html.xslFile = 'xsl/scent.xsl'
      }
    }

The reports can also be configured through chained access of the properties:

    scent {
      ...
      reports.xml.destination = 'myreport.xml'
    }

Note that if the XML report isn't enabled, the `scent` task will not run. This means that the task
can be skipped by adding the configuration line

    scent.reports.xml.enabled = false

### Dependency configuration

The Scent plugin adds a `scent` dependency configuration to the project. This configuration
specifies the default classpath for the `scent` task. By default, this configuration has one
dependency, equivalent to:

    scent 'org.myire:scent:<toolVersion>'

where `<toolVersion>` is the value of the `scent` task's `toolVersion` property.


## Reports Dashboard Plugin

The Reports Dashboard plugin creates a one-page HTML report containing a summary of some other
reports generated during a build. It is somewhat similar to the standard Gradle Build Dashboard
plugin, but it produces a much richer summary than simply adding links to all available reports.
This comes at the price of needing to have explicit knowledge about the reports it summarizes. This
means that the plugin cannot produce a summary for any unknown reports generated during a build.

### Usage

    apply plugin: 'org.myire.quill.dashboard'

### Task

The plugin adds a `reportsDashboard` task to the project. This task finalizes the `build` task,
meaning that executing the `build` task will trigger the execution of the `reportsDashboard` task.
The `reportsDashboard` task does however **not** depend on any of the tasks that create the reports
it summarizes. Only those reports that are available when the task is executed will be included in
the dashboard. This is by design; if this was not the case it wouldn't be possible to rerun only one
of the tasks that creates a summarized report and update the dashboard with this new report only.

The task creates a single HTML report. By default this report is called "reportsDashboard.html" and
is located in the project's report directory. This report can be configured like any normal Gradle
report, e.g. to give it another name:

    reportsDashboard {
      ...
      reports {
        html.destination = "$buildDir/reports/summary.html"
      }
    }

### Report sections

The HTML report created by the `reportsDashboard` task contains one *section* for each summarized
report. Each section is identified by its name, and holds the following properties:

* The report to summarize.
* An optional report to link to in the summary, for example a more detailed version of the summary.
* An XSL file to apply to the summarized report to create the HTML snippet for the section. This
implies that the report to summarize must be an XML report.

By default, the report contains sections for the following tasks:

* All tasks of type `Test` that have a report with the name `junitSummaryReport` in its convention
(see the [JUnit Additions plugin](#junit-additions-plugin)). The linked report is the task's `html`
report.

* All tasks of type `JacocoReport`. The summarized report is the task's `xml` report and the linked
report is the task's `html` report.

* All tasks of type `CoberturaReportsTask` (see the [Cobertura plugin](#cobertura-plugin)). The
summarized report is the task's `xml` report and the linked report is the task's `html` report.

* All tasks of type `ScentTask` (see the [Scent plugin](#scent-plugin)). The summarized report is
the task's `xml` report and the linked report is the task's `html` report.

* The `jdependMain` task, if present. The summarized report is the task's `xml` report and the
linked report is the task's XSL transformation report, if one exists (see the
[JDepend Additions plugin](#jdepend-additions-plugin)).

* The `spotbugsMain` task, if present. The summarized report is the task's `xml` report and the
linked report is the task's XSL transformation report, if one exists (see the
[SpotBugs Additions plugin](#spotbugs-additions-plugin)).

* The `checkstyleMain` task, if present. The summarized report is the task's `xml` report and the
linked report is the task's XSL transformation report (see the
[Checkstyle Additions plugin](#checkstyle-additions-plugin)) or, if that report hasn't been added to
the task, the task's `html` report.

* The `pmdMain` task, if present. The summarized report is the task's `xml` report and the linked
report is the task's XSL transformation report (see the
[PMD Additions plugin](#pmd-additions-plugin)) or, if that report hasn't been added to the task, the
task's `html` report.

* All tasks of type `CpdTask` that have "xml" as the format for the primary report (see the
[CPD plugin](#cpd-plugin)). The linked report is the task's `html` report.

Note that these default report sections use XSL files bundled with the Quill jar, and thus appear to
have no XSL file configured.

### Configuring the report sections

The report sections can be customized through the `sections` property. This property is a
`LinkedHashMap` with the sections' names as keys. The default sections use the name of the task that
produces the XML report as section name.

An existing report section's XSL file can be changed:

    reportsDashboard {
        sections['scent']?.xslFile = 'src/main/resources/xsl/scent_summary.xsl'
        ....
    }

Its is also possible to remove a default section:

    reportsDashboard {
        sections.remove('test')
        ....
    }

New sections can be added with the method `addSection`:

    reportsDashboard {
        addSection('pmdTest',              // Section name
                   pmdTest.reports.xml,    // Summarized report
                   pmdTest.reports.html,   // Linked report
                   'src/main/resources/xsl/pmd_summary.xsl')
        ....
    }

The detailed report can be omitted when adding a new section:

    reportsDashboard {
        addSection('pmdTest',
                   pmdTest.reports.xml,
                   'src/main/resources/xsl/pmd_summary.xsl')
        ....
    }

A section based on the report(s) produced by a task for which there is built-in support can be added
by passing the task to the `addBuiltInSection` method:

    reportsDashboard {
        addBuiltInSection(checkstyleTest)
        ...
    }

The sections will appear in the order they were added to the `LinkedHashMap`. The default sections
are added in the order they are listed above. New sections will thereby appear last in the report.
One way to rearrange the default sections is to remove a report and add it back:

    reportsDashboard {
        def testsection = sections.remove('test')
        sections.put('test', testsection)
        ....
    }

### Report layout

The HTML report contains the following parts:

* _The header_, which is the entire `<head>` tag, the opening `<body>` tag and any HTML code that
should appear before the sections. The default layout has a `<head>` tag containing the CSS style
sheet expected by the XSL files distributed with the Quill jar, and the opening `<body>` tag is
followed by a headline on the format "<project name> build report summary".

* _The sections_, which are output in a logical matrix with a configurable number of columns. This
matrix, each of its rows and each of its cells are all enclosed in an opening and an closing HTML
snippet, see below. The HTML snippet for each cell, i.e. report section, is created by applying the
XSL transformation to the XML report.

* _The footer_, which least must contain the closing `</body>` and `</html>` tags. It may also
contain additional HTML code that should appear after the sections.

### Configuring the report layout

The `layout` property of the `reportsDashboard` task is used to configure the report layout. It has
the following nested properties:

* `numColumns` - an int specifying the number of columns in the sections matrix. Default is 2.

* `headlineHtmlCode` - a string with an HTML snippet to put between the header and the sections.
Default is `<p class="headline">project-name build reports summary</p>`, where "project-name" is the
project's name..

* `sectionsStartHtmlCode` - a string with an HTML snippet to put directly before the sections.
Default is `<table width="100%">`.

* `sectionsEndHtmlCode` - a string with an HTML snippet to put directly after the sections.
Default is `</table>`.

* `rowStartHtmlCode` - a string with an HTML snippet to put directly before each row in the sections
matrix. Default is `<tr>`.

* `rowEndHtmlCode` - a string with an HTML snippet to put directly after each row in the sections
matrix. Default is `</tr>`.

* `cellStartHtmlCode` - a string with an HTML snippet to put directly before each section in the
sections matrix. Default is `<td valign="top" width="x%">` where "x" is 100/`numColumns`. For
instance, if `numColumns` is 3, each table cell will have a width of 33%.

* `cellEndHtmlCode` - a string with an HTML snippet to put directly after each section in the
sections matrix. Default is `</td>`.

* `headerHtmlFile` - a file with the header of the HTML report. This file contains the HTML code
that should appear before the headline. The minimum is the opening `<html>` and `<body>` tags. Note
that the XSL code used by the default report sections expect the  `<head>` tag, which is part of the
header, to contain the CSS style sheet used by those transformations. Default is no file, meaning
that an internally created HTML snippet should be used.

* `footerHtmlFile` - a file with the footer of the HTML report. This file contains the HTML code
that should appear after the sections. The minimum is the closing `</body>` and `</html>` tags.
Default is no file, meaning that an internally created HTML snippet should be used.

Examples:

To make the sections matrix only occupy 80% of its container's width:

    reportsDashboard {
        layout.sectionsStartHtmlCode = '<table width="80%">'
        ...
    }

To make all report sections have gray background:

    reportsDashboard {
        layout {
            rowStartHtmlCode = '<tr bgcolor="#999999">'
        }
        ...
    }

To make all report sections align with the bottom of each other:

    reportsDashboard {
        layout {
            cellStartHtmlCode = '<td valign="bottom">'
        }
        ...
    }

To add a timestamp after the sections:

    reportsDashboard {
        def tstamp = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())
        layout.sectionsEndHtmlCode = "</table><p>${tstamp}</p>"
        ...
    }


## Pom Plugin

The Pom plugin applies the Maven plugin to the project and adds a task for creating pom files
outside the context of uploading to a Maven repository.

### Usage

    apply plugin: 'org.myire.quill.pom'

### Task

The plugin adds a task with the name `createPom` to the project. This task has a single property,
`destination`, that specifies the location of the pom file to create. The default name of this file
is `${project.archivesBaseName}-${project.version}.pom`, and it is located in the directory
specified in the project property `mavenPomDir`, which is added by the Maven plugin.

The destination of the pom file can be configured by setting the destination property:

    createPom.destination = 'alternative-pom.xml'

The destination will be resolved relative to the project build directory.

The task creates the pom file by creating a `MavenPom` instance using the factory method `pom` added
by the Maven plugin. This instance is then written to the destination file.

Without any configuration, the task is equivalent to

    pom().writeTo "$mavenPomDir/${project.archivesBaseName}-${project.version}.pom"

#### The `from` method

The task can add data to the pom file from one or more XML files. These files are specified through
the method `from`, which takes one or more objects as parameters. These objects are resolved to
files relative to the project directory.

Example:

    createPom.from 'meta/pom-template.xml'

will add data from a file called `pom-template.xml` in a subdirectory of the project directory
called `meta`.

A file passed to `from` should contain a partial pom file inside a root `<project>` element.
Everything inside the root element will be added to the created pom file.

If the file `pom-template.xml` in the example has the following contents:

    <?xml version="1.0"?>
    <project>
      <name>Quill</name>
      <description>A collection of Gradle plugins</description>
      <url>https://github.com/handmadecode/quill</url>
    </project>

then the XML

      <name>Quill</name>
      <description>A collection of Gradle plugins</description>
      <url>https://github.com/handmadecode/quill</url>

will be added to the created pom file.

This method returns the `createPom` task instance to allow method chaining.

#### The `withoutScope` method

The pom created by the `pom()` method includes all dependencies. Sometimes it is not desirable to
have all dependencies in the resulting pom file. It may for instance be unnecessary to include the
test dependencies.

The `withoutScope` method takes one or more scope names as parameters. The dependencies in these
scopes will be filtered out from the created pom file.

Example:

    createPom.withoutScope 'test'

will filter out all test dependencies.

This method returns the `createPom` task instance to allow method chaining.


## Module Info Plugin

The Module Info plugin adds functionality for creating modular jar files for Java 8 projects. It
adds a task for compiling a module declaration and another task for updating the modular jar file
with a `ModuleMainClass` attribute in the `module-info.class` entry.

### Usage

    apply plugin: 'org.myire.quill.moduleinfo'

### The compileModuleInfo task

The plugin adds a `compileModuleInfo` task to the project. This task is of type `JavaCompile` and by
default has the file 'src/main/module-info/module-info.java' as its only source. This default
location can be changed by setting the `source` property:

    compileModuleInfo.source = 'path/to/module-info.java'

The task's `sourceCompatibility` and `targetCompatibility` properties are both set to '1.9' by
default, since compiling a module declaration requires at least Java 9. The  default value for the
`destinationDir` property is taken from the `compileJava` task. This means that the
'module-info.class' file will be put into the same location as the main java class files, and that
it thereby will be included in the jar artifact, making that artifact a modular jar file.

The `compileModuleInfo` task has a `moduleVersion` property in addition to the ones inherited from
`JavaCompile`. The value of this property, which defaults to `project.version`, will be used for the
module version string in the `module-info` class. If this value is set to null, no version string
will be compiled into the `module-info` class.

The plugin adds the `compileModuleInfo` task to the `jar` task's dependencies. If the
[Java Additions plugin](#java-additions-plugin) has been applied to the project, the `sourcesJar`
task is modified to include the `compileModuleInfo` task's `source`.

### The moduleMainClass task

The plugin adds a `moduleMainClass` task to the project. This task updates the jar artifact with a
`ModuleMainClass` attribute in the `module-info.class` entry.  The value for this attribute is taken
from the task's `className` property. If that attribute has no value, the value of the property
`projectMetaData.mainClass` is used. Should none of those values be present, the task will not
execute.

The `moduleMainClass` task depends on the `jar` task and is added to the `assemble` task's
dependencies.

### Requirements

The tasks added by the plugin require a Java toolchain with version 9 or greater, since a module
declaration must be compiled with at least JDK 9. Running Gradle with Java 9 is supported starting
with Gradle version 4.2.1.


## Cobertura Plugin

The Cobertura plugin adds functionality for generating Cobertura test coverage reports to all tasks
of type `Test`.

Although the plugin offers quite a few configuration options, the default values should be fine for
most use cases.

Note that the latest release of Cobertura is from February 2015, and that it doesn't work with
classes compiled for Java 11 or newer (the tool can however still be run with Java 11). Consider
using the [JaCoCo Additions plugin](#jacoco-additions-plugin) instead for code coverage. Because of
this, the Cobertura plugin is *not* applied by the 'all' plugin or by the 'core' plugin; it must
always be applied explicitly.

### Usage

    apply plugin: 'org.myire.quill.cobertura'

### Cobertura documentation

The tasks added by the plugin delegate their work to the corresponding Ant task in the Cobertura
distribution. Consequently, the majority of the properties used to configure the plugin's extensions
and tasks are the same as the ones used to configure the
[Cobertura Ant tasks](https://github.com/cobertura/cobertura/wiki/Ant-Task-Reference).

### Project extension

The plugin adds an extension with the name `cobertura` to the Gradle project. This extension is used
to configure the behaviour of all Cobertura enhanced tasks through the properties listed below.

* `toolVersion` - a string specifying the version of Cobertura to use. Default is version "2.1.1".

* `workDir` - a `File` specifying the global working directory for the Cobertura tasks. Default is a
directory called `cobertura` in the Gradle project's temporary directory.

* `coberturaClassPath` - a `FileCollection` specifying the classpath containing the Cobertura
classes used by the tasks. Default is the `cobertura` dependency configuration (see below).

* `ignoreTrivial` - a `boolean` that if true specifies that constructors/methods that contain one
line of code should be excluded from the test coverage analysis. Examples of such methods are
constructors only calling a super constructor, and getters/setters. Default is false.

* `ignoreMethodNames` - a list of strings where each string is a regular expression specifying
methods names that should be excluded from the coverage analysis. Note that the classes containing
the methods will still be instrumented. Default is an empty list. Example: `.*PrintStream.*`. Note
that the corresponding Ant task property is called `ignore`.

* `ignoreMethodAnnotations` - a list of strings where each string is the fully qualified name of an
annotations with which methods that should be excluded from the coverage analysis are annotated
with. Default is an empty list.

* `sourceEncoding` - a string with the name of the encoding that the report task(s) should use when
reading the source files. The platform's default encoding will be used if this property isn't
specified.

### Task specific project extensions

In addition to the global project extension described above, another project extension is added by
the plugin for each `Test` task that is enhanced with Cobertura functionality. The name of a task
specific extension is `cobertura` + *the capitalized name of the task*. For example, the extension
corresponding to the `test` task will have the name `coberturaTest`.

A task specific extension is used to configure the enhancement of the `Test` task, including the
related instrumentation and report tasks (see below). Each task specific extension has the following
properties:

* `enabled` - a `boolean` specifying whether the Cobertura enhancement of the `Test` task is enabled
or not. If the enhancement is disabled, the tests are run with the original classes under test, not
the instrumented ones, and no coverage report will produced for the test run. Default is true,
meaning that the tests will run with instrumented classes and that a coverage report will be
produced.

* `workDir` - a `File` specifying the working directory for the enhanced task. Default is a
directory with the same name as the enhanced `Test` task in the directory specified by `workDir` in
the global project extension.

* `inputClasses` - a `FileCollection` specifying the classes to analyze for test coverage. Default
is all files in the output classes directory of the main source set. Used as input by the
instrumentation task.

* `auxClassPath` - a `FileCollection` specifying a path containing any classes that shouldn't be
analyzed but are needed by the instrumentation. Default is no auxiliary class path. Used as input by
the instrumentation task.

* `ignoreTrivial` - overrides `ignoreTrivial` in the global project extension if set. Used as input
by the instrumentation task.

* `ignoreMethodNames` - overrides `ignoreMethodNames` in the global project extension if set. Used
as input by the instrumentation task.

* `ignoreMethodAnnotations` - overrides `ignoreMethodAnnotations` in the global project extension if
set. Used as input by the instrumentation task.

* `instrumentedClassesDir` - a `File` specifying the directory containing the instrumented versions
of the classes to analyze. Default is a directory named `instrumented` in the directory specified by
`workDir`. Used as output by the instrumentation task and as input by the test task.

* `instrumentationDataFile` - a `File` specifying the file holding metadata about the instrumented
classes. This file contains information about the names of the classes, their method names, line
numbers, etc. It is created by the instrumentation task. The default value is a file named
`cobertura.instrumentation.ser` in the directory specified by `workDir`. Used as output by the
instrumentation task and input by the test task.

* `executionDataFile` - a `File` specifying the file holding metadata about the test execution of
the instrumented classes. This file contains updated information about the instrumented classes from
the test runs. It is an updated version of `instrumentationDataFile`, created by the test task and
used as input by the report task. The default value is a file named `cobertura.execution.ser` in
the directory specified by `workDir`.

* `sourceDirs` - a `FileCollection` specifying the directories containing the sources of the
analyzed classes. Default is all source directories in the main source set. Used as input by the
report task.

* `sourceEncoding` - overrides `sourceEncoding` in the global project extension if set. Used as
input by the report task.

### Instrumentation tasks

The plugin adds an instrumentation task for each enhanced `Test` task. The name of this task is
the name of the `Test` task + `CoberturaInstrument`, e.g. `testCoberturaInstrument` for the `test`
task.

An instrumentation task gets its properties from the corresponding task specific project extension.
When executed, the instrumentation task instruments the classes in `inputClasses` and writes the
instrumented versions of those classes to `instrumentedClassesDir`. The task also creates the
`instrumentationDataFile` file.

### Enhancement of Test tasks

The plugin adds two actions to each enhanced `Test` task.

The first action is added to the beginning of the task's action list. This action prepares the test
execution by prepending the `instrumentedClassesDir` and the `coberturaClassPath` to the test task's
classpath. It also set the system property `net.sourceforge.cobertura.datafile` in the test task's
forked JVM to the value of `executionDataFile`, and copies `instrumentationDataFile` to
`executionDataFile`.

The second action is added to the end of the task's action list. This action restores the test task
by setting its classpath and `net.sourceforge.cobertura.datafile` system property to the values they
had before the preparing action was run.

The instrumentation task associated with an enhanced `Test` task is added to the latter's
dependencies.

### Report tasks

The plugin adds an report task for each enhanced `Test` task. The name of this task is `cobertura` +
the name of the `Test` task + `Report`, e.g. `coberturaTestReport` for the `test` task. This means
that it normally is sufficient to specify `cobertura` as the task to execute.

A report task gets most of its properties from the corresponding task specific project extension,
see above. In addition to these, it has a read-only property `reports` that holds two report
specifications:

* `xml` - a `SingleFileReport` specifying the XML report file. Default is a file called
`coverage.xml` in a directory with the same name as the associated enhanced `Test` task in a
directory called `cobertura` in the Gradle project's report directory.

* `html` - a `DirectoryReport` specifying the directory where the HTML report is created. Default is
a directory with the same name as the associated enhanced `Test` task in a directory called
`cobertura` in the Gradle project's report directory.

These two reports can be configured as any Gradle report, e.g.

    coberturaTestReport.reports.xml.enabled = false
    coberturaTestReport.reports.html.destination = "${project.buildDir}/reports/coverage"

A report task depends on the associated enhanced `Test` task.

### Dependency configuration

The Cobertura plugin adds a `cobertura` dependency configuration to the project. This configuration
specifies the default classpath for the Cobertura task. By default, this configuration has one
dependency, equivalent to:

    cobertura 'net.sourceforge.cobertura:cobertura:<toolVersion>'

where `<toolVersion>` is the value of the `cobertura` extension's `toolVersion` property.


## JDepend Additions Plugin

The JDepend Additions plugin applies the standard Gradle plugin `jdepend` to the project and
configures the corresponding project extension and tasks with some defaults and additions. It also
replaces the `jdepend` configuration's dependency on the standard JDepend distribution with a
dependency on the [guru-nidi](https://github.com/nidi3/jdepend) fork.

The `jdepend` plugin was deprecated in Gradle version 5 and is scheduled to be removed in Gradle
version 6. Because of this, the JDepend Additions plugin is *not* applied by the 'all' plugin or by
the 'core' plugin; it must always be applied explicitly.

### Usage

    apply plugin: 'org.myire.quill.jdepend'

### Configuration dependencies

The latest version of the JDepend tool is 2.9.1, which was released in 2005. This version does not
recognize the Java 8 class format, and it omits classes with Java 8 specific constructs (e.g. method
handles) from the analysis.

To support dependency analysis of Java 8 code, the plugin uses the
[guru-nidi](https://github.com/nidi3/jdepend) fork of JDepend rather than the standard distribution.
This is done by replacing the `jdepend` configuration's dependency on the `jdepend:jdepend` artifact
with a dependency on the `guru.nidi:jdepend` artifact. The version of the `guru.nidi:jdepend`
artifact is specified through the extension property `guruNidiVersion` (see below).

This replacement of the standard JDepend library can be disabled by setting the `guruNidiVersion`
property to null.

### Default values

The plugin configures the `jdepend` extension in the project to let the build continue even if
failures occur. This currently is a no-op as JDepend does not have the concept of failure; the
property is an inheritance from `CodeQualityExtension`. This is equivalent to specifying the
following in the build script:

    jdepend.ignoreFailures = true

### Extension additions

The plugin adds a method called `disableTestChecks` to the `jdepend` extension. Calling this method
in the build script it will remove the `test` source set from the extension's source sets, thus
disabling the `jdependTest` task:

    jdepend.disableTestChecks()

The plugin also adds two properties:

* `guruNidiVersion` - a string specifying the version of the guru-nidi JDepend fork to use. Default
is version "2.9.5". If this property is set to null, the guru-nidi fork will *not* be used.

* `antTaskVersion` - a string specifying the version of the JDepend Ant task to use in conjunction
with the guru-nidi fork. Default is "1.9.7". Note that this property does not affect the version of
the Ant task used when the guru-nidi fork is disabled.

### Task additions

The plugin adds an [XSL transformation report](#xsl-transformation-reports) to all tasks of type
`JDepend`. These reports have the name `quillHtmlReport` and are `enabled` by default. They can be
configured per task, e.g. to use another XSL file than the one distributed in the Quill jar:

    jdependMain {
      quillHtmlReport.xslFile = 'resources/jdepend.xsl'
      quillHtmlReport.destination = "$buildDir/reports/tools/jdepend.html"
    }

    jdependTest.quillHtmlReport.enabled = false

The plugin also adds a read-only property `jdependProperties` to all tasks of type `JDepend`. This
property allows specifying a properties file with runtime options for JDepend, as described in the
[documentation](http://clarkware.com/software/JDepend.html#customize). Normally this can only be
achieved by adding the properties file to the JDepend classpath (which is what the plugin does if a
properties file is specified for the task).

The properties file resolved relative to the project directory and is configured like

    jdependMain {
        jdependProperties.file = 'jdepend.properties'
    }

If no properties file is explicitly configured the plugin uses a file bundled with the Quill jar
that excludes the `java.lang` package from the JDepend analysis:

    ignore.java=java.lang

This file is extracted to the path "tmp/jdepend/jdepend.properties" relative to the project's build
directory.
