package org.jenkinsci.plugins.artepo.repo.svn;

import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

public class SvnRepo extends AbstractRepo {
    private String svnUrl;
    private String svnUser;
    private String svnPassword;

    @DataBoundConstructor
    public SvnRepo(String svnUrl, String svnUser, String svnPassword) {
        this.svnUrl = svnUrl;
        this.svnUser = svnUser;
        this.svnPassword = svnPassword;
    }

    public SvnRepo() {
        this(null, null, null);
    }

    public String getSvnUrl() {
        return svnUrl;
    }

    public void setSvnUrl(String svnUrl) {
        this.svnUrl = svnUrl;
    }

    public String getSvnUser() {
        return svnUser;
    }

    public void setSvnUser(String svnUser) {
        this.svnUser = svnUser;
    }

    public String getSvnPassword() {
        return svnPassword;
    }

    public void setSvnPassword(String svnPassword) {
        this.svnPassword = svnPassword;
    }

    @Override
    protected AbstractRepoImpl createImpl(RepoInfoProvider infoProvider) {
        return new SvnRepoImpl(infoProvider, svnUrl, svnUser, svnPassword);
    }
}
