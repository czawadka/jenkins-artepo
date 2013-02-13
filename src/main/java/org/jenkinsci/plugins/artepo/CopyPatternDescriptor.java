package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.Descriptor;

@Extension
public class CopyPatternDescriptor extends Descriptor<CopyPattern> {

    public CopyPatternDescriptor() {
        super(CopyPattern.class);
    }

    @Override
    public String getDisplayName() {
        return "";
    }
}
