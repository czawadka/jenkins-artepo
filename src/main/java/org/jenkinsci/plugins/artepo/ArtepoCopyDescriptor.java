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
        return "Artepo Copy";
    }

    public List<RepoDescriptor> getRepoDescriptors() {
        return AbstractRepo.all();
    }

    public AbstractRepo getDefaultRepo() throws FormException {
        return AbstractRepo.getDefaultRepo();
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        Repo destinationRepo = newRepoInstance(req, formData, "destinationRepo");
        List<SourcePattern> sourcePatterns = req.bindJSONToList(SourcePattern.class,
                formData.get("patterns"));
        String sourcePromotionName = formData.getString("sourcePromotionName");

        return new ArtepoCopy(destinationRepo, sourcePatterns, sourcePromotionName);
    }

    private Repo newRepoInstance(StaplerRequest req, JSONObject formData, String repoName)
            throws FormException {
        JSONObject repoFormData = formData.getJSONObject(repoName);
        String repoType = repoFormData.getString("value");
        RepoDescriptor repoDescriptor = AbstractRepo.getDescriptorByType(repoType);
        return repoDescriptor!=null ? repoDescriptor.newInstance(req, repoFormData) : null;
    }
}
