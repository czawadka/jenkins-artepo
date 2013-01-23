package org.jenkinsci.plugins.artepo.repo.svn;

import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

public class SvnRepo extends AbstractRepo {
    private String url;
    private String user;
    private String password;

    @DataBoundConstructor
    public SvnRepo(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public SvnRepo() {
        this(null, null, null);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected AbstractRepoImpl createImpl(RepoInfoProvider infoProvider) {
        return new SvnRepoImpl(infoProvider, url, user, password);
    }

    @Override
    public String toString() {
        return "svn repo "+ url;
    }
}
