package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.ScmRepo;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class SvnRepo extends ScmRepo {
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
    public boolean checkout(FilePath dstPath, String buildTag) throws InterruptedException, IOException {
        return false;
    }

    @Override
    public boolean commit(FilePath srcPath, String buildTag) throws InterruptedException, IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
