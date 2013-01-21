package org.jenkinsci.plugins.artepo.repo;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Hudson;

abstract public class AbstractRepo implements Describable<AbstractRepo>, Repo {

    public RepoDescriptor getDescriptor() {
        return (RepoDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    static public DescriptorExtensionList<AbstractRepo,RepoDescriptor> all() {
        return Hudson.getInstance().<AbstractRepo,RepoDescriptor>getDescriptorList(AbstractRepo.class);
    }

    static public RepoDescriptor getDescriptorByType(String type) {
        for(RepoDescriptor descriptor : all()) {
            if (descriptor.getType().equals(type))
                return descriptor;
        }
        return null;
    }

    static public AbstractRepo getDefaultRepo() {
        DescriptorExtensionList<AbstractRepo,RepoDescriptor> repoDescriptors = all();
        if (repoDescriptors.isEmpty())
            return null;
        RepoDescriptor repoDescriptor = repoDescriptors.get(0);
        try {
            return repoDescriptor.clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}