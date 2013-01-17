package org.jenkinsci.plugins.artepo.repo;

import hudson.DescriptorExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Hudson;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.BackupSource;

import java.io.IOException;
import java.util.List;

abstract public class Repo implements Describable<Repo> {
    transient protected AbstractBuild<?, ?> build;
    transient protected Launcher launcher;
    transient protected FilePath tempPath;
    transient protected BuildListener listener;

    public boolean backup(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, FilePath tempPath,
                          FilePath sourcePath, String buildTag, List<BackupSource> backupSources)
            throws InterruptedException, IOException {

        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        this.tempPath = tempPath;

        try {
            return backup(sourcePath, buildTag, backupSources);
        } finally {
            this.build = null;
            this.launcher = null;
            this.listener = null;
            this.tempPath = null;
        }
    }

    abstract protected boolean backup(FilePath sourcePath, String buildTag, List<BackupSource> backupSources)
            throws InterruptedException, IOException;

    public RepoDescriptor getDescriptor() {
        return (RepoDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    public String getType() {
        return getDescriptor().getType();
    }

    public static DescriptorExtensionList<Repo,RepoDescriptor> all() {
        return Hudson.getInstance().<Repo,RepoDescriptor>getDescriptorList(Repo.class);
    }

    public static RepoDescriptor getDescriptorByType(String type) {
        for(RepoDescriptor descriptor : all()) {
            if (descriptor.getType().equals(type))
                return descriptor;
        }
        return null;
    }

    public static Repo getDefaultRepo() {
        DescriptorExtensionList<Repo,RepoDescriptor> repoDescriptors = all();
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

    public FilePath getTempPath() {
        return tempPath;
    }
}
