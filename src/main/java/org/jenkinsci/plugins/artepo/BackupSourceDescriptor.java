package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.Descriptor;

@Extension
public class BackupSourceDescriptor extends Descriptor<BackupSource> {

    public BackupSourceDescriptor() {
        super(BackupSource.class);
    }

    @Override
    public String getDisplayName() {
        return "";
    }
}
