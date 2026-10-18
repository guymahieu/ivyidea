<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IvyIDEA Changelog

## [Unreleased]

### Added
- "IvyIDEA: Resolve for All Modules" as a "Before launch" task for run configurations (thanks to Eric Jarosch)
- IntelliJ now watches the Ivy files, Ivy settings files and properties files of the IvyIDEA modules. After you edit one of them, a button appears in the editor to resolve all modules again, like the one for Maven and Gradle projects. After changes outside IntelliJ, such as a checkout, all modules are resolved automatically. This can be changed in Settings | Build, Execution, Deployment | Build Tools (thanks to Eric Jarosch)
- An option to store the resolved libraries as project libraries instead of module libraries. New projects use project libraries by default (thanks to Eric Jarosch)
- The paths of the Ivy file and the Ivy settings file can now contain path macros, like `$MODULE_DIR$` and `$PROJECT_DIR$`, and path variables. A relative path is relative to the module directory, or to the project directory in the project settings
- An option to resolve the modules in parallel when resolving all modules, with a configurable number of threads (thanks to Stijn Vranckx)
- An option to ignore artifact types (default: pom). Artifacts of these types are no longer downloaded or reported as "Unrecognized artifact type" after a resolve
- An option to clear the IvyIDEA workspace cache to File | Invalidate Caches

### Changed
- Upgraded internal Apache Ivy to 2.6.0
- The minimum supported IntelliJ IDEA version is now 2023.3
- New icons that match the look of IntelliJ's new UI, also on the libraries and ivy files of IvyIDEA modules
- Other IvyIDEA modules in the project are now resolved from their local ivy.xml, so their transitive dependencies no longer come from an outdated published version (thanks to Stijn Vranckx)
- Faster updating of module libraries after a resolve on large projects (thanks to Stijn Vranckx)
- Per-artifact messages are no longer logged at INFO level in idea.log

### Fixed
- A resolve now removes the dependencies on other IvyIDEA modules that the ivy.xml doesn't declare. Dependencies on modules without IvyIDEA are kept (thanks to Xeonkryptos)
- "Always attach sources/javadocs" no longer attaches artifacts that IntelliJ can't read as an archive

## [1.0.19]
- The action "Resolve for current module" was no longer available. This has been fixed.

## [1.0.18]
- Upgraded Apache Ivy to 2.5.2 to address the CVE-2022-46751 vulnerability
- rebased build on [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Fixed deprecation warning about ActionUpdateThread.OLD_EDT in IntelliJ IDEA 2024.2

## [1.0.17]
- Upgraded Apache Ivy to 2.5.1 to address the CVE-2022-37865 and CVE-2022-37866 vulnerabilities

## [1.0.16]
- Upgraded internal Apache Ivy to 2.5.0
- Removed deprecated API usages

## [1.0.15]
- Removed deprecated API usages
- Added checkbox "Detect dependencies on other modules in the same project". Disabling this option will force IvyIDEA to resolve dependecies only through the ivy.xml ignoring internal modules. (thanks to Lorenzo Bertacchi)

## [1.0.14]
- When trying to resolve dependencies without an Ivy settings file, an IllegalArgumentException was thrown when clicking on the 'Open Project Settings' link

## [1.0.13]

## [1.0.12]
- The configurations to resolve are now stored alphabetically in the .iml file
- Modified files are now saved before starting to resolve the dependencies

## [1.0.11]
- Fixed compatibility issue with IntelliJ 11
- Upgraded internal Apache Ivy to 2.4.0 (including dependencies)

[Unreleased]: https://github.com/guymahieu/ivyidea/compare/1.0.19...HEAD
[1.0.19]: https://github.com/guymahieu/ivyidea/releases/tag/1.0.19
[1.0.18]: https://github.com/guymahieu/ivyidea/releases/tag/1.0.18
[1.0.17]: https://github.com/guymahieu/ivyidea/releases/tag/1.0.17
[1.0.16]: https://github.com/guymahieu/ivyidea/releases/tag/1.0.16
[1.0.15]: https://github.com/guymahieu/ivyidea/releases/tag/1.0.15
[1.0.14]: https://github.com/guymahieu/ivyidea/releases/tag/1.0.14
[1.0.13]: https://github.com/guymahieu/ivyidea/releases/tag/1.0.13
[1.0.12]: https://github.com/guymahieu/ivyidea
[1.0.11]: https://github.com/guymahieu/ivyidea
