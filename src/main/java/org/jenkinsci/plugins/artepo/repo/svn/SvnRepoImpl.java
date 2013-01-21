package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.BackupSource;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.IOException;
import java.util.List;

public class SvnRepoImpl {
    RepoInfoProvider infoProvider;
    SVNURL svnUrl;
    String svnUser;
    String svnPassword;
    SvnHelper svnHelper;

    public SvnRepoImpl(RepoInfoProvider infoProvider, String svnUrl, String svnUser, String svnPassword) {
        this.infoProvider = infoProvider;
        this.svnUser = svnUser;
        this.svnPassword = svnPassword;
        try {
            this.svnUrl = SVNURL.parseURIDecoded(svnUrl);
        } catch (SVNException e) {
            throw new IllegalArgumentException("Error parsing "+svnUrl, e);
        }

        this.svnHelper = createSvnHelper();
    }

    SvnHelper createSvnHelper() {
        SvnHelper svnHelper = new SvnHelper();
        svnHelper.setAuthentication(svnUser, svnPassword);
        svnHelper.setEventHandler(new ISVNEventHandler() {
            public void handleEvent(SVNEvent event, double progress) throws SVNException {
                infoProvider.getLogger().println(event.toString());
            }

            public void checkCancelled() throws SVNCancelException {
                if (!infoProvider.isBuildActive())
                    throw new SVNCancelException();
            }
        });
        return svnHelper;
    }

    public FilePath prepareSource(String buildTag) throws InterruptedException, IOException {
        try {

            FilePath wcPath = checkout(buildTag);
            return wcPath;

        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    FilePath checkout(String buildTag)
            throws SVNException, IOException, InterruptedException {

        SVNRevision revision = findRevisionFromBuildTag(buildTag);
        createRepositoryFolderIfNotExists();

        FilePath wcPath = getWCPath();
        svnHelper.checkout(ArtepoUtil.toFile(wcPath), svnUrl, SVNRevision.HEAD);

        return wcPath;
    }

    SVNRevision findRevisionFromBuildTag(String buildTag) {
        if (buildTag==null)
            return SVNRevision.HEAD;
        throw new UnsupportedOperationException("findRevisionFromBuildTag");
    }

    void createRepositoryFolderIfNotExists() throws SVNException {
        SVNInfo svnInfo = svnHelper.info(svnUrl);
        if (svnInfo==null) {
            svnHelper.mkdir(svnUrl);
        }
    }

    FilePath getWCPath() {
        String nameFromUrl = svnUrl.toString().replaceAll("[^0-9a-zA-Z]+", "_");
        return infoProvider.getTempPath().child(nameFromUrl);
    }

    public void copyFrom(FilePath source, List<BackupSource> patterns, String buildTag)
            throws InterruptedException, IOException {
        try {

            FilePath wcPath = checkout(null);
            ArtepoUtil.sync(wcPath, source, patterns);
            commit(wcPath, buildTag);

        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    void commit(FilePath wcPath, String buildTag) throws SVNException, IOException, InterruptedException {
        svnHelper.deleteMissingAddUnversioned(ArtepoUtil.toFile(wcPath));

        String commitMessage = prepareCommitMessage(buildTag);
        svnHelper.commit(ArtepoUtil.toFile(wcPath), commitMessage);
    }

    String prepareCommitMessage(String buildTag) {
        return "buildnumber: "+buildTag;
    }

}
