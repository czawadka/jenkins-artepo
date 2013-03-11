package org.jenkinsci.plugins.artepo;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class CopyPattern implements Describable<CopyPattern>, Serializable {
    private String subFolder;
    private String includes;
    private String excludes;

    @DataBoundConstructor
    public CopyPattern(String subFolder, String includes, String excludes) {
        this.subFolder = subFolder;
        this.includes = includes;
        this.excludes = excludes;
    }

    public CopyPattern() {
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
        String subFolder = this.subFolder !=null && this.subFolder.length()>0 ? this.subFolder +"/" : "";
        String includes = this.includes!=null ? this.includes : "";
        String excludes = this.excludes!=null && this.excludes.length()>0 ? "{-" + this.excludes +'}' : "";
        return subFolder
                + includes
                + excludes;
    }

    public Descriptor<CopyPattern> getDescriptor() {
        return Hudson.getInstance().getDescriptor(getClass());
    }

}
