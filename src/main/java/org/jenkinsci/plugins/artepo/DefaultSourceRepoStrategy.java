package org.jenkinsci.plugins.artepo;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Project;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.workspace.WorkspaceRepo;

public class DefaultSourceRepoStrategy implements SourceRepoStrategy {

    public Repo getSourceRepo(ArtepoCopy requester, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        ArtepoCopy projectArtepo = (ArtepoCopy) ((Project)build.getProject()).getPublisher(requester.getDescriptor());
        if (projectArtepo!=null && projectArtepo!=requester)
            return projectArtepo.getDestinationRepo();
        else
            return new WorkspaceRepo();
    }
}
