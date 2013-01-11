package org.jenkinsci.plugins.artepo.repo;

import hudson.model.Descriptor;

public abstract class RepoDescriptor extends Descriptor<Repo> {
    private String type;

    protected RepoDescriptor(Class<? extends Repo> clazz, String type) {
        super(clazz);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}