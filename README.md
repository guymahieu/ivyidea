# ivyidea
<!-- Plugin description -->
Resolves module dependencies through Ivy

Features:
- Apache Ivy integration (up to v2.5.2); no need for external ant build scripts to be called
- Automatic ivy configuration of modules using facets (for modules containing an ivy.xml file)
- Detection of dependencies that are really other intellij modules in the same project; these are added as module references
- Detect source/document/jar type ivy artifacts in dependencies and add them as such to the module
- Creation of a module library with all resolved ivy dependencies
- Ivy configurations that need to be resolved can be chosen for each module
- Properties can be injected into the ivy resolve process
<!-- Plugin description end -->

https://github.com/guymahieu/ivyidea/blob/wiki/ProjectHome.md