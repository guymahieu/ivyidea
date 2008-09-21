package org.clarent.ivyidea.ivy;

import org.apache.ivy.Ivy;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.clarent.ivyidea.config.PostIvyPluginConfiguration;

/**
 * @author Guy Mahieu
 */

public class IvyWrapper {

    private static IvyWrapper instance = new IvyWrapper();

    private Ivy ivy;

    private IvyWrapper() {
        try {
            this.ivy = Ivy.newInstance();
            this.ivy.configure(new File(PostIvyPluginConfiguration.getCurrent().getIvyConfFile()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static IvyWrapper getInstance() {
        return instance;
    }

    public Ivy getIvy() {
        return ivy;
    }
}
