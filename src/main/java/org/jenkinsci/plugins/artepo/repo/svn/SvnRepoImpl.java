package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.SourcePattern;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.BuildTagNotFoundException;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.IOException;
import java.util.List;

public class SvnRepoImpl extends AbstractRepoImpl {
    String svnUrl;
    String svnUser;
    String svnPassword;
    SvnHelper svnHelper;

    public SvnRepoImpl(RepoInfoProvider infoProvider, String svnUrl, String svnUser, String svnPassword) {
        super(infoProvider);

        this.svnUrl = svnUrl;
        this.svnUser = svnUser;
        this.svnPassword = svnPassword;

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
            return checkout(buildTag);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    FilePath checkout(String buildTag)
            throws SVNException, IOException, InterruptedException {

        SVNURL url = SVNURL.parseURIDecoded(svnUrl);
        createRepositoryFolderIfNotExists(url);

        FilePath wcPath = getWCPath();
        SVNRevision revision = buildTag==null ? SVNRevision.HEAD : findRevisionFromBuildTag(buildTag);
        svnHelper.checkout(ArtepoUtil.toFile(wcPath), url, revision);

        return wcPath;
    }

    SVNRevision findRevisionFromBuildTag(String buildTag) throws SVNException {
        SVNRevision currentRevision = SVNRevision.HEAD;
        SVNURL url = SVNURL.parseURIEncoded(svnUrl);
        int limit = 20;

        String pattern = prepareCommitMessage(buildTag);

        do {
            List<SVNLogEntry> logEntries = svnHelper.log(url, currentRevision, limit);
            for (SVNLogEntry logEntry : logEntries) {
                String commitMessage = logEntry.getMessage();
                currentRevision = SVNRevision.create(logEntry.getRevision());
                if (commitMessage!=null && commitMessage.contains(pattern))
                    return currentRevision;
            }
        } while(currentRevision.getNumber()>0);

        throw new BuildTagNotFoundException(buildTag, svnUrl);
    }

    void createRepositoryFolderIfNotExists(SVNURL url) throws SVNException {
        SVNInfo svnInfo = svnHelper.info(url);
        if (svnInfo==null) {
            svnHelper.mkdir(url);
        }
    }

    FilePath getWCPath() {
        String nameFromUrl = svnUrl.replaceAll("[^0-9a-zA-Z]+", "_");
        return infoProvider.getTempPath().child(nameFromUrl);
    }

    public void copyFrom(FilePath source, List<SourcePattern> patterns, String buildTag)
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

    public String getSvnUrl() {
        return svnUrl;
    }

    public String getSvnUser() {
        return svnUser;
    }

    public String getSvnPassword() {
        return svnPassword;
    }
}
