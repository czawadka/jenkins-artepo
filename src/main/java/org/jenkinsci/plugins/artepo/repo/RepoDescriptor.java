package org.jenkinsci.plugins.artepo.repo;

import hudson.model.Descriptor;

abstract public class RepoDescriptor extends Descriptor<AbstractRepo> {
    private String type;

    protected RepoDescriptor(Class<? extends AbstractRepo> clazz, String type) {
        super(clazz);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}