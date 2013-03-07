package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;

import java.io.IOException;
import java.io.PrintStream;

public interface RepoInfoProvider {
    public boolean isBuildActive();
    public FilePath getTempPath() throws IOException, InterruptedException;
    public PrintStream getLogger();
    public FilePath getWorkspacePath();
}
