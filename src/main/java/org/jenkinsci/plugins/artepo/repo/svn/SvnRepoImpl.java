package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.CopyPattern;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.BuildNotFoundException;
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
import java.util.regex.Pattern;

public class SvnRepoImpl extends AbstractRepoImpl {
    String url;
    String user;
    String password;
    SvnHelper svnHelper;

    public SvnRepoImpl(RepoInfoProvider infoProvider, String url, String user, String password) {
        super(infoProvider);

        this.url = url;
        this.user = user;
        this.password = password;

        this.svnHelper = createSvnHelper();
    }

    Pattern endsWithSlash = Pattern.compile("/+$");
    Pattern startsWithSlash = Pattern.compile("^/+");

    SvnHelper createSvnHelper() {
        SvnHelper svnHelper = new SvnHelper();
        svnHelper.setAuthentication(user, password);
        final int urlBaseLength = url==null ? 0 : endsWithSlash.matcher(url).replaceAll("").length();
        svnHelper.setEventHandler(new ISVNEventHandler() {
            public void handleEvent(SVNEvent event, double progress) throws SVNException {
                String path = event.getURL()!=null ? event.getURL().toString().substring(urlBaseLength) : null;
                if (path!=null)
                    path = startsWithSlash.matcher(path).replaceAll("");
                infoProvider.getLogger().println(event.getAction() + " " + (path!=null ? path : ""));
            }

            public void checkCancelled() throws SVNCancelException {
                if (!infoProvider.isBuildActive())
                    throw new SVNCancelException();
            }
        });
        return svnHelper;
    }

    public FilePath prepareSource(int buildNumber) throws InterruptedException, IOException {
        try {
            return checkout(buildNumber);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    FilePath checkout(Integer buildNumber)
            throws SVNException, IOException, InterruptedException {

        infoProvider.getLogger().println("Checkout from "+url);

        SVNURL svnUrl = SVNURL.parseURIDecoded(this.url);
        createRepositoryFolderIfNotExists(svnUrl);

        FilePath wcPath = getWCPath();
        SVNRevision revision = buildNumber==null ? SVNRevision.HEAD : findRevisionFromBuildTag(buildNumber);
        svnHelper.checkout(ArtepoUtil.toFile(wcPath), svnUrl, revision);

        return wcPath;
    }

    SVNRevision findRevisionFromBuildTag(int buildNumber) throws SVNException {
        SVNRevision currentRevision = SVNRevision.HEAD;
        SVNURL svnUrl = SVNURL.parseURIEncoded(this.url);
        int limit = 20;

        String pattern = prepareCommitMessage(buildNumber);

        do {
            List<SVNLogEntry> logEntries = svnHelper.log(svnUrl, currentRevision, limit);
            for (SVNLogEntry logEntry : logEntries) {
                String commitMessage = logEntry.getMessage();
                currentRevision = SVNRevision.create(logEntry.getRevision());
                if (commitMessage!=null && commitMessage.contains(pattern))
                    return currentRevision;
            }
        } while(currentRevision.getNumber()>0);

        throw new BuildNotFoundException(buildNumber, this.url);
    }

    void createRepositoryFolderIfNotExists(SVNURL svnUrl) throws SVNException {
        SVNInfo svnInfo = svnHelper.info(svnUrl);
        if (svnInfo==null) {
            svnHelper.mkdir(svnUrl);
        }
    }

    FilePath getWCPath() throws IOException, InterruptedException {
        String nameFromUrl = url.replaceAll("[^0-9a-zA-Z]+", "_");
        return infoProvider.getTempPath().child(nameFromUrl);
    }

    public void copyFrom(FilePath source, CopyPattern pattern, int buildNumber)
            throws InterruptedException, IOException {
        try {

            FilePath wcPath = checkout(null);
            sync(wcPath, source, pattern);
            commit(wcPath, buildNumber);

        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    void commit(FilePath wcPath, int buildNumber) throws SVNException, IOException, InterruptedException {
        infoProvider.getLogger().println("Commit to "+url);

        svnHelper.deleteMissingAddUnversioned(ArtepoUtil.toFile(wcPath));

        String commitMessage = prepareCommitMessage(buildNumber);
        svnHelper.commit(ArtepoUtil.toFile(wcPath), commitMessage);
    }

    String prepareCommitMessage(int buildNumber) {
        return "buildnumber: "+buildNumber;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
