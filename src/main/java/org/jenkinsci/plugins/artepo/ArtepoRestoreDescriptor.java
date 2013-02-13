package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoDescriptor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;

@Extension
public class ArtepoRestoreDescriptor extends BuildStepDescriptor<Publisher> {

    public ArtepoRestoreDescriptor() {
        super(ArtepoRestore.class);
        load();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        // disable artepo restore for root level project because it is pointless
        // to restore artifacts - they exists in workspace already
        boolean isMainProject = Project.class.isAssignableFrom(jobType);
        return !isMainProject;
    }

    @Override
    public String getDisplayName() {
        return "Artepo Restore";
    }

    public List<RepoDescriptor> getRepoDescriptors() {
        return AbstractRepo.all();
    }

    public AbstractRepo getDefaultRepo() throws FormException {
        return AbstractRepo.getDefaultRepo();
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        String sourcePromotionName = formData.getString("sourcePromotionName");

        return new ArtepoRestore(sourcePromotionName);
    }
}
