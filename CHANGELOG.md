<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IvyIDEA Changelog

## Unreleased
### Added
- rebased build on [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

## 1.0.16
- Upgraded internal Apache Ivy to 2.5.0
- Removed deprecated API usages

## 1.0.15
- Removed deprecated API usages
- Added checkbox "Detect dependencies on other modules in the same project". Disabling this option will force IvyIDEA to resolve dependecies only through the ivy.xml ignoring internal modules. (thanks to Lorenzo Bertacchi)
                

## 1.0.14
- When trying to resolve dependencies without an Ivy settings file, an IllegalArgumentException was thrown when clicking on the 'Open Project Settings' link

## 1.0.13

## 1.0.12
- The configurations to resolve are now stored alphabetically in the .iml file
- Modified files are now saved before starting to resolve the dependencies

## 1.0.11
- Fixed compatibility issue with IntelliJ 11
- Upgraded internal Apache Ivy to 2.4.0 (including dependencies)

