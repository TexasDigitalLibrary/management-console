package org.duracloud.sync.endpoint;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author: Bill Branan
 * Date: Apr 9, 2010
 */
public class TestDuraStoreChunkSyncEndpoint extends DuraStoreSyncTestBase {

    @Test
    public void testDuraStoreChunkSyncEndpoint() throws Exception {
        int maxFileSize = 1024;

        DuraStoreChunkSyncEndpoint endpoint =
            new DuraStoreChunkSyncEndpoint(host,
                                           Integer.parseInt(port),
                                           context,
                                           rootCredential.getUsername(),
                                           rootCredential.getPassword(),
                                           spaceId,
                                           maxFileSize);
        testSync(endpoint);

        File largeFile = File.createTempFile("large", "file", tempDir);
        FileOutputStream fos = new FileOutputStream(largeFile);

        int filesize = maxFileSize + maxFileSize/2;
        for(int i=0;  i< filesize; i++) {
            fos.write(i);
        }
        fos.flush();
        fos.close();

        endpoint.syncFile(largeFile, tempDir);
        testEndpoint(endpoint, 3);
    }
}