package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.Repo;
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
        Repo sourceRepo = newRepoInstance(req, formData, "sourceRepo");
        Repo destinationRepo = newRepoInstance(req, formData, "destinationRepo");
        List<BackupSource> sourcePatterns = req.bindJSONToList(BackupSource.class,
                formData.get("sourcePatterns"));

        return new ArtepoCopy(sourceRepo, destinationRepo, sourcePatterns);
    }

    private Repo newRepoInstance(StaplerRequest req, JSONObject formData, String repoName)
            throws FormException {
        JSONObject repoFormData = formData.getJSONObject(repoName);
        String repoType = repoFormData.getString("value");
        RepoDescriptor repoDescriptor = AbstractRepo.getDescriptorByType(repoType);
        return repoDescriptor!=null ? repoDescriptor.newInstance(req, repoFormData) : null;
    }
}
