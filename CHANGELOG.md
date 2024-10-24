<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IvyIDEA Changelog

## [Unreleased]

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
