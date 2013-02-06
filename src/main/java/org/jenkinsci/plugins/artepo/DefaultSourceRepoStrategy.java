package org.jenkinsci.plugins.artepo;

import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotionProcess;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.workspace.WorkspaceRepo;

import java.util.List;

/**
 * Source repo is destination repo from main artepo build step. If we are in main artepo
 * workspace repo will be returned
 */
public class DefaultSourceRepoStrategy implements SourceRepoStrategy {

    public Repo getSourceRepo(ArtepoCopy requester, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        AbstractProject rootProject = build.getProject().getRootProject();
        Repo repo;
        if ((repo=findByPromotionName(rootProject, requester))!=null) {
            listener.getLogger().println("Found source artepo ("+repo+") in '"+requester.getSourcePromotionName()+"' promotion");
            return repo;
        }
        if ((repo=findSourceRepoByMainArtepo(rootProject, requester))!=null) {
            listener.getLogger().println("Found source artepo ("+repo+") as main artepo");
            return repo;
        }
        repo = new WorkspaceRepo();
        listener.getLogger().println("Found default source artepo ("+repo+")");
        return repo;
    }

    protected Repo findByPromotionName(AbstractProject project, ArtepoCopy requester) {
        JobPropertyImpl promotionProperty = (JobPropertyImpl) project.getProperty(JobPropertyImpl.class);
        if (promotionProperty==null)
            return null;
        String sourcePromotionName = requester.getSourcePromotionName();
        if (sourcePromotionName==null)
            return null;
        PromotionProcess promotionProcess = promotionProperty.getItem(sourcePromotionName.trim());
        if (promotionProcess==null)
            return null;
        List<ArtepoCopy> artepos = Util.createSubList(promotionProcess.getBuildSteps(),ArtepoCopy.class);
        if (artepos.isEmpty())
            return null;
        return artepos.get(0).getDestinationRepo();
    }

    protected Repo findSourceRepoByMainArtepo(AbstractProject project, ArtepoCopy requester) {
        ArtepoCopy artepo = (ArtepoCopy) ((Project)project).getPublisher(Jenkins.getInstance().getDescriptor(ArtepoCopy.class));
        if (artepo==null || artepo==requester)
            return null;
        return artepo.getDestinationRepo();
    }
}
