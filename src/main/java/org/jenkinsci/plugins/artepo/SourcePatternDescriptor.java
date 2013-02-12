package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.Descriptor;

@Extension
public class SourcePatternDescriptor extends Descriptor<CopyPattern> {

    public SourcePatternDescriptor() {
        super(CopyPattern.class);
    }

    @Override
    public String getDisplayName() {
        return "";
    }
}
