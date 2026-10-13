/*
 * Copyright 2010 Guy Mahieu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.clarent.ivyidea.config;

import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.clarent.ivyidea.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles the file names in the IvyIDEA settings: expands the IntelliJ path macros in them, like
 * {@code $MODULE_DIR$}, {@code $PROJECT_DIR$}, {@code $USER_HOME$} and the path variables, and converts them between
 * the form in which they are stored, shown and loaded.
 * <p>
 * IntelliJ already expands the macros it knows when it loads the settings, but not in a path that was entered in the
 * settings since then, nor $PROJECT_DIR$ in a module file.
 */
public final class IvyIdeaPaths {

    private static final Pattern MACRO = Pattern.compile("\\$([A-Za-z0-9_.-]+)\\$");
    private static final Pattern DOUBLED_SEPARATORS = Pattern.compile("/{2,}");

    private IvyIdeaPaths() {
    }

    /**
     * Expands the path macros in the given path from the settings of the given module. A relative path is resolved
     * against the module directory.
     */
    @Nullable
    public static String expand(@NotNull Module module, @Nullable String path) {
        // the macros of a module don't include $PROJECT_DIR$
        final PathMacroManager moduleMacros = PathMacroManager.getInstance(module);
        final PathMacroManager projectMacros = PathMacroManager.getInstance(module.getProject());
        return expand(path, p -> projectMacros.expandPath(moduleMacros.expandPath(p)), ModuleUtilCore.getModuleDirPath(module));
    }

    /**
     * Expands the path macros in the given path from the project settings. A relative path is resolved against the
     * project directory.
     */
    @Nullable
    public static String expand(@NotNull Project project, @Nullable String path) {
        final PathMacroManager projectMacros = PathMacroManager.getInstance(project);
        return expand(path, projectMacros::expandPath, project.getBasePath());
    }

    /**
     * Expands the macros in the given path with the given expander, and resolves a relative path against the given
     * directory. URLs are only expanded. Blank paths are returned as is.
     */
    @Nullable
    static String expand(@Nullable String path, @NotNull UnaryOperator<String> macroExpander, @Nullable String baseDir) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        final String expanded = macroExpander.apply(path.trim());
        if (isUrl(expanded)) {
            return expanded;
        }
        // an undefined path variable stays in the path; the error then shows it
        if (!new File(expanded).isAbsolute() && !StringUtils.isBlank(baseDir) && findMacro(expanded) == null) {
            return FileUtil.toCanonicalPath(new File(baseDir, expanded).getPath());
        }
        return FileUtil.toCanonicalPath(expanded);
    }

    /**
     * Returns the name of the first macro left in the given expanded path, which is a path variable that isn't
     * defined, or null if there is none.
     */
    @Nullable
    public static String findMacro(@Nullable String path) {
        if (path == null) {
            return null;
        }
        final Matcher matcher = MACRO.matcher(path);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static boolean isUrl(@NotNull String path) {
        return path.contains("://");
    }

    /**
     * Converts a file URL from the settings to a URL for Java. An IntelliJ file URL with a Windows drive, like
     * {@code file://C:/dev/ivysettings.xml}, which is what {@code file://$PROJECT_DIR$/ivysettings.xml} expands to, is
     * converted to {@code file:/C:/dev/ivysettings.xml}: Java reads the drive letter in it as a host name. Other file
     * URLs, like {@code file:///C:/dev/ivysettings.xml} or {@code file://server/share/ivysettings.xml}, are already
     * URLs for Java.
     */
    @NotNull
    public static URL toUrl(@NotNull String fileUrl) throws MalformedURLException {
        final String path = VfsUtilCore.urlToPath(fileUrl);
        if (startsWithWindowsDrive(path)) {
            // a hierarchical URL, against which Ivy can resolve the files the settings include
            return new File(path).toURI().toURL();
        }
        return new URL(fileUrl);
    }

    static boolean startsWithWindowsDrive(@NotNull String path) {
        return path.length() >= 2 && path.charAt(1) == ':' && Character.isLetter(path.charAt(0));
    }

    /**
     * Converts a path entered in the settings to the form in which it is stored. IntelliJ only replaces paths with
     * forward slashes by macros when it saves the settings.
     */
    @Nullable
    public static String toStored(@Nullable String path) {
        if (path == null) {
            return null;
        }
        final String trimmed = path.trim();
        return isUrl(trimmed) ? trimmed : toForwardSlashes(trimmed);
    }

    /**
     * Replaces the backslashes in the given path by forward slashes, and doubled separators, like in a path copied from
     * a Java properties file ({@code C:\\dev\\ivy.xml}), by a single one. The double slash of a UNC path is kept.
     */
    @NotNull
    static String toForwardSlashes(@NotNull String path) {
        final String independent = FileUtil.toSystemIndependentName(path);
        final String uncPrefix = independent.startsWith("//") ? "//" : "";
        return uncPrefix + DOUBLED_SEPARATORS.matcher(independent.substring(uncPrefix.length())).replaceAll("/");
    }

    /**
     * Converts a stored path to the form in which it is shown in the settings: a path that starts with a macro keeps
     * the forward slashes with which it is stored, other paths get the separators of the operating system.
     */
    @Nullable
    public static String toDisplayed(@Nullable String path) {
        if (path == null || isUrl(path) || MACRO.matcher(path).lookingAt()) {
            return path;
        }
        return FileUtil.toSystemDependentName(path);
    }

    @NotNull
    public static List<String> toStored(@NotNull List<String> paths) {
        return paths.stream().map(IvyIdeaPaths::toStored).collect(Collectors.toList());
    }

    @NotNull
    public static List<String> toDisplayed(@NotNull List<String> paths) {
        return paths.stream().map(IvyIdeaPaths::toDisplayed).collect(Collectors.toList());
    }
}
