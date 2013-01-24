package org.jenkinsci.plugins.artepo;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.artepo.repo.Repo;

public interface SourceRepoStrategy {
    public Repo getSourceRepo(ArtepoCopy requester, AbstractBuild<?, ?> build, Launcher launcher,
                              BuildListener listener);
}
