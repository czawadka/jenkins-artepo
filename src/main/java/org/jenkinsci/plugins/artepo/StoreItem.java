package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class StoreItem extends AbstractDescribableImpl<StoreItem> {
    private String dir;
    private String includes;
    private String excludes;

    @DataBoundConstructor
    public StoreItem(String dir, String includes, String excludes) {
        this.dir = dir;
        this.includes = includes;
        this.excludes = excludes;
    }

    public StoreItem() {
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
        return "StoreItem{" +
                "dir='" + dir + '\'' +
                ", includes='" + includes + '\'' +
                ", excludes='" + excludes + '\'' +
                '}';
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<StoreItem> {
        public DescriptorImpl() {
            super(StoreItem.class);
        }

        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
