package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;

import java.io.PrintStream;

public interface RepoInfoProvider {
    public boolean isBuildActive();
    public FilePath getTempPath();
    public PrintStream getLogger();
    public FilePath getWorkspacePath();
}
