package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import hudson.model.Result;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.BackupSource;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.kohsuke.stapler.DataBoundConstructor;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.IOException;
import java.util.List;

public class SvnRepo extends Repo {
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
    protected boolean backup(FilePath srcPath, String buildTag, List<BackupSource> backupSources) throws InterruptedException, IOException {
        try {
            initSvn();
            FilePath wcPath = getWCPath("backup");

            createRepositoryFolderIfNotExists();

            svnHelper.checkout(ArtepoUtil.toFile(wcPath), parsedSvnUrl, SVNRevision.HEAD);

            ArtepoUtil.sync(wcPath, srcPath, backupSources);
            svnHelper.deleteMissingAddUnversioned(ArtepoUtil.toFile(wcPath));

            String commitMessage = prepareCommitMessage(buildTag);
            svnHelper.commit(ArtepoUtil.toFile(wcPath), commitMessage);

            return true;
        } catch(SVNException e) {
            throw new RuntimeException(e);
        } finally {
            deinitSvn();
        }
    }

    private String prepareCommitMessage(String buildTag) {
        return "buildnumber: "+buildTag;
    }

    transient SvnHelper svnHelper;
    transient SVNURL parsedSvnUrl;

    private void initSvn() throws SVNException {
        parsedSvnUrl = SVNURL.parseURIEncoded(svnUrl);

        svnHelper = new SvnHelper();
        svnHelper.setAuthentication(svnUser, svnPassword);
        svnHelper.setEventHandler(new ISVNEventHandler() {
            public void handleEvent(SVNEvent event, double progress) throws SVNException {
                listener.getLogger().println(event.toString());
            }

            public void checkCancelled() throws SVNCancelException {
                if (build.getResult()== Result.ABORTED)
                    throw new SVNCancelException();
            }
        });
    }

    private void createRepositoryFolderIfNotExists() throws SVNException {
        SVNInfo svnInfo = svnHelper.info(parsedSvnUrl);
        if (svnInfo==null) {
            svnHelper.mkdir(parsedSvnUrl);
        }
    }

    private FilePath getWCPath(String type) {
        String nameFromUrl = svnUrl.replaceAll("[^0-9a-zA-Z]+", "_");
        return getTempPath().child(nameFromUrl);
    }

    private void deinitSvn() {
        parsedSvnUrl = null;
        if (svnHelper!=null) {
            svnHelper.dispose();
            svnHelper = null;
        }
    }
}
