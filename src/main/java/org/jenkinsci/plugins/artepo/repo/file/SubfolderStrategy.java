package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public enum SubfolderStrategy {

    OnlyBuildSubfolders() {
        @Override
        public List<FilePath> getDestinationPaths(FilePath repoPath, int buildNumber) throws InterruptedException, IOException {
            return Arrays.asList(
                    getFormattedBuildSubfolder(repoPath, buildNumber)
            );
        }

        @Override
        public List<FilePath> getPotentialSourcePaths(FilePath repoPath, int buildNumber) throws InterruptedException, IOException {
            return Arrays.asList(
                    getFormattedBuildSubfolder(repoPath, buildNumber),
                    getUnFormattedBuildSubfolder(repoPath, buildNumber)
            );
        }
    },

    BuildAndLatestSubfolders() {
        @Override
        public List<FilePath> getDestinationPaths(FilePath repoPath, int buildNumber) throws InterruptedException, IOException {
            return Arrays.asList(
                    getFormattedBuildSubfolder(repoPath, buildNumber),
                    repoPath.child("latest")
            );
        }

        @Override
        public List<FilePath> getPotentialSourcePaths(FilePath repoPath, int buildNumber) throws InterruptedException, IOException {
            return Arrays.asList(
                    getFormattedBuildSubfolder(repoPath, buildNumber)
            );
        }
    },

    NoSubfolders() {
        @Override
        public List<FilePath> getDestinationPaths(FilePath repoPath, int buildNumber) throws InterruptedException, IOException {
            return Arrays.asList(
                    repoPath
            );
        }

        @Override
        public List<FilePath> getPotentialSourcePaths(FilePath repoPath, int buildNumber) throws InterruptedException, IOException {
            return Arrays.asList(
                    repoPath
            );
        }
    };

    static public final SubfolderStrategy DEFAULT = OnlyBuildSubfolders;

    abstract public List<FilePath> getDestinationPaths(FilePath repoPath, int buildNumber) throws InterruptedException, IOException;
    abstract public List<FilePath> getPotentialSourcePaths(FilePath repoPath, int buildNumber) throws InterruptedException, IOException;


    static public FilePath getFormattedBuildSubfolder(FilePath repoPath, int buildNumber) {
        return repoPath.child(formatBuildNumber(buildNumber));
    }
    static public FilePath getUnFormattedBuildSubfolder(FilePath repoPath, int buildNumber) {
        return repoPath.child(String.valueOf(buildNumber));
    }

    static private String formatBuildNumber(int buildNumber) {
        return String.format("%05d", buildNumber);
    }

}
