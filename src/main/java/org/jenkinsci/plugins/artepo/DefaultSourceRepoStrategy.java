package org.jenkinsci.plugins.artepo;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Project;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.workspace.WorkspaceRepo;

/**
 * Source repo is destination repo from main artepo build step. If we are in main artepo
 * workspace repo will be returned
 */
public class DefaultSourceRepoStrategy implements SourceRepoStrategy {

    public Repo getSourceRepo(ArtepoCopy requester, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        AbstractProject rootProject = build.getProject().getRootProject();
        ArtepoCopy projectArtepo = (ArtepoCopy) ((Project)rootProject).getPublisher(requester.getDescriptor());
        if (projectArtepo!=null && projectArtepo!=requester)
            return projectArtepo.getDestinationRepo();
        else
            return new WorkspaceRepo();
    }
}
