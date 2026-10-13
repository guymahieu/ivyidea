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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import org.clarent.ivyidea.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Expands the IntelliJ path macros, like {@code $MODULE_DIR$}, {@code $PROJECT_DIR$}, {@code $USER_HOME$} and the
 * path variables, in the file names of the IvyIDEA settings.
 * <p>
 * IntelliJ already expands the macros when it loads the settings, but not in a path that was entered in the settings
 * since then, nor a path variable that isn't defined.
 */
public final class IvyIdeaPaths {

    private static final Pattern MACRO = Pattern.compile("\\$([A-Za-z0-9_.-]+)\\$");

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
     * Replaces the start of the given path from the settings of the given module by a path macro, to show it in the
     * settings. IntelliJ stores the path with the macro, but expands it when it loads the settings.
     */
    @Nullable
    public static String collapse(@NotNull Module module, @Nullable String path) {
        return collapse(path, Arrays.asList(
                collapser(PathMacroManager.getInstance(module)),
                collapser(PathMacroManager.getInstance(module.getProject())),
                collapser(PathMacroManager.getInstance(ApplicationManager.getApplication()))));
    }

    /**
     * Replaces the start of the given path from the project settings by a path macro, to show it in the settings.
     */
    @Nullable
    public static String collapse(@NotNull Project project, @Nullable String path) {
        return collapse(path, Arrays.asList(
                collapser(PathMacroManager.getInstance(project)),
                collapser(PathMacroManager.getInstance(ApplicationManager.getApplication()))));
    }

    @NotNull
    private static UnaryOperator<String> collapser(@NotNull PathMacroManager macros) {
        return p -> macros.collapsePath(p, false);
    }

    /**
     * Collapses the given path with the first of the given collapsers that replaces a directory containing the path,
     * so a module directory comes before the project directory, and the project directory before the user home.
     * A result like {@code $MODULE_DIR$/../ivysettings.xml} is skipped. URLs and blank paths are returned as is.
     */
    @Nullable
    static String collapse(@Nullable String path, @NotNull List<UnaryOperator<String>> collapsers) {
        if (StringUtils.isBlank(path) || isUrl(path) || findMacro(path) != null) {
            return path;
        }
        final String independentPath = FileUtil.toSystemIndependentName(path.trim());
        for (UnaryOperator<String> collapser : collapsers) {
            final String collapsed = collapser.apply(independentPath);
            if (!collapsed.equals(independentPath) && !collapsed.contains("$/..")) {
                return collapsed;
            }
        }
        return path;
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
     * Converts a path entered in the settings to the form in which it is stored. IntelliJ only replaces paths with
     * forward slashes by macros when it saves the settings.
     */
    @Nullable
    public static String toStored(@Nullable String path) {
        if (path == null) {
            return null;
        }
        final String trimmed = path.trim();
        return isUrl(trimmed) ? trimmed : FileUtil.toSystemIndependentName(trimmed);
    }

    /**
     * Converts a stored path to the form in which it is shown in the settings.
     */
    @Nullable
    public static String toDisplayed(@Nullable String path) {
        if (path == null || isUrl(path)) {
            return path;
        }
        return FileUtil.toSystemDependentName(path);
    }

    @NotNull
    public static List<String> toStored(@NotNull List<String> paths) {
        return paths.stream().map(IvyIdeaPaths::toStored).collect(Collectors.toList());
    }

    /**
     * Converts a path from the settings of the given module to the form in which it is shown in the settings, with
     * a path macro where possible.
     */
    @Nullable
    public static String toDisplayed(@NotNull Module module, @Nullable String path) {
        return toDisplayed(collapse(module, path));
    }

    /**
     * Converts a path from the project settings to the form in which it is shown in the settings, with a path macro
     * where possible.
     */
    @Nullable
    public static String toDisplayed(@NotNull Project project, @Nullable String path) {
        return toDisplayed(collapse(project, path));
    }

    @NotNull
    public static List<String> toDisplayed(@NotNull Module module, @NotNull List<String> paths) {
        return paths.stream().map(path -> toDisplayed(module, path)).collect(Collectors.toList());
    }

    @NotNull
    public static List<String> toDisplayed(@NotNull Project project, @NotNull List<String> paths) {
        return paths.stream().map(path -> toDisplayed(project, path)).collect(Collectors.toList());
    }
}
