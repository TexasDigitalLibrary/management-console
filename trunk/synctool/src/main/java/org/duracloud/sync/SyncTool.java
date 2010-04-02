package org.duracloud.sync;

import org.duracloud.sync.backup.SyncBackupManager;
import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.sync.config.SyncToolConfigParser;
import org.duracloud.sync.endpoint.DuraStoreSyncEndpoint;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.duracloud.sync.mgmt.StatusManager;
import org.duracloud.sync.mgmt.SyncManager;
import org.duracloud.sync.monitor.DirectoryUpdateMonitor;
import org.duracloud.sync.walker.DirWalker;
import org.duracloud.sync.walker.RestartDeleteChecker;
import org.duracloud.sync.walker.RestartDirWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Starting point for the Sync Tool. The purpose of this tool is to synchronize
 * all of the files in a given set of local file system directories with a
 * space in DuraCloud. This means that as files are added, updated, and deleted
 * locally, the Sync Tool will perform the same activities on the files within
 * DuraCloud.
 *
 * When the Sync Tool is started for the first time, it will consider all local
 * files under the given directories (recursively) and determine if those files
 * already exist in the DuraCloud space. If not, the files will be added. At
 * that point, the Sync Tool will monitor for updates on the local file system
 * directories and make updates as needed.
 *
 * If the Sync Tool is turned off or exits for some reason, and is started again
 * pointing to the same backup directory it will load its previous state and
 * look through the local file system for files which have changed since it
 * performed its last backup, which it will then sync with DuraCloud.
 *
 * @author: Bill Branan
 * Date: Mar 11, 2010
 */
public class SyncTool {

    private final Logger logger = LoggerFactory.getLogger(SyncTool.class);
    private SyncToolConfig syncConfig;
    private SyncManager syncManager;
    private SyncBackupManager syncBackupManager;
    private DirectoryUpdateMonitor dirMonitor;
    private SyncEndpoint syncEndpoint;

    private SyncToolConfig processCommandLineArgs(String[] args) {
        SyncToolConfigParser syncConfigParser = new SyncToolConfigParser();
        syncConfig = syncConfigParser.processCommandLine(args);
        syncConfigParser.printConfig(syncConfig);
        return syncConfig;
    }

    private void startSyncManager() {
        syncEndpoint = new DuraStoreSyncEndpoint(syncConfig.getHost(),
                                                 syncConfig.getPort(),
                                                 syncConfig.getContext(),
                                                 syncConfig.getUsername(),
                                                 syncConfig.getPassword(),
                                                 syncConfig.getSpaceId());
        syncManager = new SyncManager(syncConfig.getSyncDirs(),
                                      syncEndpoint,
                                      syncConfig.getNumThreads(),
                                      syncConfig.getPollFrequency());
        syncManager.beginSync();
    }

    private long startSyncBackupManager() {
        syncBackupManager =
            new SyncBackupManager(syncConfig.getBackupDir(),
                                  syncConfig.getPollFrequency());
        long lastBackup = syncBackupManager.attemptRestart();
        syncBackupManager.startupBackups();
        return lastBackup;
    }

    private void startDirWalker() {
        DirWalker dirWalker = new DirWalker(syncConfig.getSyncDirs());
        dirWalker.walkDirs();
    }

    private void startRestartDirWalker(long lastBackup) {
        RestartDirWalker reDirWalker =
            new RestartDirWalker(syncConfig.getSyncDirs(), lastBackup);
        reDirWalker.walkDirs();
    }

    private void startDeleteChecker() {
        RestartDeleteChecker deleteChecker = new RestartDeleteChecker();
        deleteChecker.runDeleteChecker(syncEndpoint.getFilesList(),
                                       syncConfig.getSyncDirs());
    }

    private void startDirMonitor() {
        dirMonitor = new DirectoryUpdateMonitor(syncConfig.getSyncDirs(),
                                                syncConfig.getPollFrequency());
        dirMonitor.startMonitor();
    }

    private void listenForExit() {
        StatusManager statusManager = StatusManager.getInstance();
        BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;
        while(!exit) {
            String input;
            try {
                input = br.readLine();
                if(input.equalsIgnoreCase("exit") ||
                   input.equalsIgnoreCase("x")) {
                    exit = true;
                } else if(input.equalsIgnoreCase("config") ||
                          input.equalsIgnoreCase("c")) {
                    System.out.println(syncConfig.getPrintableConfig());
                } else if(input.equalsIgnoreCase("status") ||
                          input.equalsIgnoreCase("s")) {
                    System.out.println(statusManager.getPrintableStatus());
                }
            } catch(IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        closeSyncTool();
    }

    private void closeSyncTool() {
        syncBackupManager.endBackups();
        syncManager.endSync();
        dirMonitor.stopMonitor();        
    }

    public void runSyncTool(SyncToolConfig syncConfig) {
        this.syncConfig = syncConfig;
        startSyncManager();

        long lastBackup = startSyncBackupManager();
        if(lastBackup > 0) {
            startRestartDirWalker(lastBackup);
            startDeleteChecker();
        } else {
            startDirWalker();
        }

        startDirMonitor();
        listenForExit();
    }

    public static void main(String[] args) throws Exception {
        SyncTool syncTool = new SyncTool();
        SyncToolConfig syncConfig = syncTool.processCommandLineArgs(args);
        syncTool.runSyncTool(syncConfig);
    }
}