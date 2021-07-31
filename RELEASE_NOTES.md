# Quill Release Notes

[version 3.1](#version-31)  
[version 3.0](#version-30)  
[version 2.4](#version-24)  
[version 2.3.1](#version-231)  
[version 2.3](#version-23)  
[version 2.2](#version-22)  
[version 2.1](#version-21)  
[version 2.0](#version-20)  
[version 1.5](#version-15)  
[version 1.4](#version-14)  
[version 1.3](#version-13)  
[version 1.2](#version-12)  
[version 1.1](#version-11)  
[version 1.0](#version-10)  
[version 0.11](#version-011)  
[version 0.10](#version-010)  
[version 0.9](#version-09)


### version 3.1

* Requires Gradle 6.1 to run.
* All task reports support the properties `required` and `outputLocation` in addition to the
  deprecated properties `enabled` and `destination`.
* Checkstyle default version upgraded to 8.44. The built-in configuration file requires at least
  version 8.42.
* PMD and CPD default versions upgraded to 6.36. The built-in PMD rule file requires at least
  version 6.36.
* Jacoco default version upgraded to 0.8.7.


### version 3.0

* Support for Gradle 7.
* Requires Gradle 6 to run.
* Checkstyle default version upgraded to 8.41.1. The built-in configuration file requires at least
  version 8.38.
* PMD and CPD default versions upgraded to 6.33. The built-in PMD rule file requires at least
  version 6.25.
* Jacoco default version upgraded to 0.8.6.
* Scent default version upgraded to 2.3.
* The Ivy Import plugin used Ivy version 2.5.0 by default, but does not depend on any 2.5 specific
  functionality.
* Cobertura plugin removed. Use the Jacoco plugin instead.
* JDepend Additions plugin removed (the JDepend plugin was removed in Gradle 6).


### version 2.4

* The Pom plugin is no longer based on the deprecated Maven plugin, which is scheduled to be removed
  in Gradle 7.
* Checkstyle default version upgraded to 8.32. The built-in configuration file requires at least
  version 8.31.
* PMD and CPD default versions upgraded to 6.23.


### version 2.3.1

* Jacoco default version upgraded to 0.8.5 to support running with Java 14.


### version 2.3

* [Jol plugin](./README.md#jol-plugin) added.
* Checkstyle default version upgraded to 8.30.
* PMD and CPD default versions upgraded to 6.22.
* Support for SpotBugs Gradle plugin version 4.


### version 2.2

* Support for Gradle 6.
* Requires Gradle 4.3 to run, tested up to version 6.1.
* Default `scopeToConfiguration` mapping in the
  [Maven Import plugin](./README.md#maven-import-plugin) changed to use configurations not scheduled
  for removal in Gradle 7.
* Checkstyle default version upgraded to 8.28.
* PMD and CPD default versions upgraded to 6.20.


### version 2.1

* The Quill plugins can be applied using the plugins DSL.
* Checkstyle default version upgraded to 8.25. The built-in configuration file requires at least
  version 8.24.
* Checks `JavadocBlockTagLocation` and `UnnecessarySemicolonAfterTypeMemberDeclaration` added to
  built-in Checkstyle configuration. The `NoWhitespaceAfter` check allows line breaks after an array
  initialization's opening brace.
* Scent default version upgraded to 2.2.


### version 2.0

* Requires Gradle 4 to run, tested up to version 5.5.1.
* Requires Java 8 to run. 
* The `core` plugin only applies plugins that are compatible with Java 11 language level and class
  file format.
* [JaCoCo Additions plugin](./README.md#jacoco-additions-plugin) added.
* [SpotBugs Additions plugin](./README.md#spotbugs-additions-plugin) added.
* JDepend Additions and Cobertura plugins removed from the `core` plugin.
* The Reports Dashboard task's layout related properties moved to nested `layout` property.
* Scent default version upgraded to 2.1.
* The default Maven version used by the [Maven Import plugin](./README.md#maven-import-plugin)
  upgraded to 3.6.1.
* The components of the `mavenImport` dependency configuration changed.  
* The [Ivy Import plugin](./README.md#ivy-import-plugin) (formerly Ivy Module plugin) redesigned to
  be aligned with the Maven Import plugin.
* The CPD plugin is based directly on the CPD library, not the Ant task. The minimum CPD version
  supported is 6.1, and the default version is 6.16.
* PMD default version upgraded to 6.17. The built-in rule file requires at least version 6.12 and
  the built-in XSL file for creating HTML reports requires at least version 6.0.
* Checkstyle default version upgraded to 8.23. The built-in configuration file requires at least
  version 8.22.
* Reports created by the Quill plugins are no longer added to the standard Build Dashboard plugin.  
* FindBugs Additions plugin removed. Use the SpotBugs Additions plugin instead.
* JavaNCSS plugin removed. Use the Scent plugin instead.

### version 1.5

* Gradle 4 compatibility.
* [Module Info plugin](./README.md#module-info-plugin) added.
* Property `mainClass` added to the [projectMetaData](./README.md#the-projectmetadata-extension) extension.
* Method [addMainClassAttribute](./README.md#main-class-attribute) added to the `manifest` of all `Jar` tasks.
* The `scope` property of the `JavadocMethod` check in the built-in Checkstyle configuration
changed from _private_ to  _package_.
* Scent default version upgraded to 1.0.
* Fixed incorrect group of the quill artifact in the dependency examples in this document.

### version 1.4

* The Maven Import plugin adds methods to the project that sets the group and version from a pom
  file.
* [Core plugin](./README.md#general-usage) added.
* PMD and CPD default versions upgraded to 5.5.7.

### version 1.3

* [Maven Import plugin](./README.md#maven-import-plugin) added.
* The Reports Dashboard plugin creates links to the dashboard reports of any child projects.
* PMD and CPD default versions upgraded to 5.5.2.

### version 1.2

* Quill requires at least Java 1.7 to run.
* [Scent plugin](./README.md#scent-plugin) added. It replaces the JavaNCSS plugin as the Quill standard plugin
for source code metrics since JavaNCSS does not support Java 8 code.
* The JavaNCSS plugin is no longer applied by the 'all' plugin, it must be applied explicitly.
* The Reports Dashboard no longer contains a section for JavaNCSS by default. If the JavaNCSS plugin
is applied its Reports Dashboard section must be added explicitly.
* The JDepend plugin is enhanced to use the guru-nidi fork for Java 8 support.

### version 1.1

* [Pom plugin](./README.md#pom-plugin) added.
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
