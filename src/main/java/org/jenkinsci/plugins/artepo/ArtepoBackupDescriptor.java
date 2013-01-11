package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoDescriptor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;

@Extension
public class ArtepoBackupDescriptor extends BuildStepDescriptor<Publisher> {

    public ArtepoBackupDescriptor() {
        super(ArtepoBackup.class);
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
        return Repo.all();
    }

    public Repo getDefaultRepo() throws FormException {
        return Repo.getDefaultRepo();
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        Repo repo = newRepoInstance(req, formData);
        List<BackupSource> sources = req.bindJSONToList(BackupSource.class, formData.get("sources"));

        return new ArtepoBackup(repo, sources);
    }

    private Repo newRepoInstance(StaplerRequest req, JSONObject formData) throws FormException {
        JSONObject repoFormData = formData.getJSONObject("repo");
        String repoType = repoFormData.getString("value");
        RepoDescriptor repoDescriptor = Repo.getDescriptorByType(repoType);
        Repo repo = repoDescriptor!=null ? repoDescriptor.newInstance(req, repoFormData) : null;

        return repo;
    }
}
