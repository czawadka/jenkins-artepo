package org.jenkinsci.plugins.artepo;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

public class SourcePattern implements Describable<SourcePattern> {
    private String subFolder;
    private String includes;
    private String excludes;

    @DataBoundConstructor
    public SourcePattern(String subFolder, String includes, String excludes) {
        this.subFolder = subFolder;
        this.includes = includes;
        this.excludes = excludes;
    }

    public SourcePattern() {
        this("","","");
    }

    public String getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(String subFolder) {
        this.subFolder = subFolder;
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
        return "SourcePattern[" +
                subFolder +
                ", " + includes +
                ", " + excludes +
                ']';
    }

    public Descriptor<SourcePattern> getDescriptor() {
        return Hudson.getInstance().getDescriptor(getClass());
    }

}
