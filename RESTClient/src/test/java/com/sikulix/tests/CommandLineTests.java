package com.sikulix.tests;

import com.sikulix.entities.BaseTest;
import com.sikulix.entities.CommandLineBox;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Author: Sergey Kuts
 */
public class CommandLineTests extends BaseTest {

    private static final String EMPTY_FILE_PATH = BATCHES_PATH + "\\" + EMPTY_FILE;

    private List<String> localPaths;
    private List<String> remotePaths;

    @BeforeClass
    public void init() throws IOException {
        final File runnerBatch = getResource(RESOURCE_RUNNER_BATCH);
        final File sampleBatch = getResource(RESOURCE_SAMPLE_BATCH);

        localPaths = Arrays.asList(runnerBatch.getPath(), sampleBatch.getPath());
        remotePaths = Arrays.asList(BATCHES_PATH + "\\" + runnerBatch.getName(),
                BATCHES_PATH + "\\" + sampleBatch.getName());

        if (getClient().exists(Arrays.asList(BATCHES_PATH))) {
            getClient().delete(BATCHES_PATH);
        }

        getClient().createFolder(BATCHES_PATH);
    }

    // Don't forget to change resource batches' extension from .txt to .bat
    @Test
    public void runBatchFromCommandLine() {
        getClient().uploadFile(localPaths, BATCHES_PATH);
        if (getClient().exists(remotePaths) && remotePaths.size() > 0) {
            getClient().executeCommandLine(new CommandLineBox(remotePaths.get(0), Arrays.asList("arg1", "arg2", "arg3"), WAIT_TIMEOUT));
            assertTrue(getClient().exists(Arrays.asList(EMPTY_FILE_PATH)));
        } else {
            fail();
        }
    }
}
