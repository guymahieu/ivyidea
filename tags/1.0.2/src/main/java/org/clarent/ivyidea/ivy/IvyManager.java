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

package org.clarent.ivyidea.ivy;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.trigger.Trigger;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.logging.ConsoleViewMessageLogger;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guy Mahieu
 */

public class IvyManager {

    private Map<Module, Ivy> configuredIvyInstances = new HashMap<Module, Ivy>();

    public Ivy getIvy(final Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        if (!configuredIvyInstances.containsKey(module)) {
            final IvySettings configuredIvySettings = IvyIdeaConfigHelper.createConfiguredIvySettings(module);
            final Ivy ivy = Ivy.newInstance(configuredIvySettings);
            
            // we should now call the Ivy#postConfigure() method, but it is private :-(
            // so we have to execute the same code ourselfs
            postConfigure(ivy);

            registerConsoleLogger(module, ivy);

            configuredIvyInstances.put(module, ivy);
        }
        return configuredIvyInstances.get(module);
    }

    private void postConfigure(final Ivy ivy) {
        EventManager eventManager = ivy.getEventManager();
        Collection triggers = ivy.getSettings().getTriggers();
        for (Iterator iter = triggers.iterator(); iter.hasNext();) {
            Trigger trigger = (Trigger) iter.next();
            eventManager.addIvyListener(trigger, trigger.getEventFilter());
        }
        
        for (Iterator iter = ivy.getSettings().getResolvers().iterator(); iter.hasNext();) {
            Object resolver = iter.next();
            if (resolver instanceof BasicResolver) {
                ((BasicResolver) resolver).setEventManager(eventManager);
            }
        }
    }
    
    private void registerConsoleLogger(final Module module, final Ivy ivy) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                ivy.getLoggerEngine().pushLogger(
                        new ConsoleViewMessageLogger(
                                module.getProject(),
                                IntellijUtils.getConsoleView(module.getProject())
                        )
                );
            }
        }, ModalityState.NON_MODAL);
    }

}
