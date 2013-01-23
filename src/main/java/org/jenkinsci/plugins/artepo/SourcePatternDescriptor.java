package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.Descriptor;

@Extension
public class SourcePatternDescriptor extends Descriptor<SourcePattern> {

    public SourcePatternDescriptor() {
        super(SourcePattern.class);
    }

    @Override
    public String getDisplayName() {
        return "";
    }
}
