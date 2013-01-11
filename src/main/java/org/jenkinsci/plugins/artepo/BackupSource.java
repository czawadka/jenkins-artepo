package org.jenkinsci.plugins.artepo;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

public class BackupSource implements Describable<BackupSource> {
    private String dir;
    private String includes;
    private String excludes;

    @DataBoundConstructor
    public BackupSource(String dir, String includes, String excludes) {
        this.dir = dir;
        this.includes = includes;
        this.excludes = excludes;
    }

    public BackupSource() {
        this("","","");
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        return "BackupSource[" +
                dir +
                ", " + includes +
                ", " + excludes +
                ']';
    }

    public Descriptor<BackupSource> getDescriptor() {
        return Hudson.getInstance().getDescriptor(getClass());
    }

}
