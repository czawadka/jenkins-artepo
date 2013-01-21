package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.RepoDescriptor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;

@Extension
public class ArtepoCopyDescriptor extends BuildStepDescriptor<Publisher> {

    public ArtepoCopyDescriptor() {
        super(ArtepoCopy.class);
        load();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Artepo Backup";
    }

    public List<RepoDescriptor> getRepoDescriptors() {
        return AbstractRepo.all();
    }

    public AbstractRepo getDefaultRepo() throws FormException {
        return AbstractRepo.getDefaultRepo();
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        AbstractRepo repo = newRepoInstance(req, formData);
        String buildTag = formData.getString("buildTag");
        List<BackupSource> sources = req.bindJSONToList(BackupSource.class, formData.get("sources"));

        return new ArtepoCopy(repo, buildTag, sources);
    }

    private AbstractRepo newRepoInstance(StaplerRequest req, JSONObject formData) throws FormException {
        JSONObject repoFormData = formData.getJSONObject("repo");
        String repoType = repoFormData.getString("value");
        RepoDescriptor repoDescriptor = AbstractRepo.getDescriptorByType(repoType);
        AbstractRepo repo = repoDescriptor!=null ? repoDescriptor.newInstance(req, repoFormData) : null;

        return repo;
    }
}
