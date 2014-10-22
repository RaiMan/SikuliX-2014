package com.sikulix.tests;

import com.sikulix.entities.BaseTest;
import com.sikulix.entities.ImageBox;
import org.sikuli.script.Key;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Author: Sergey Kuts
 */
public class SikuliXTests extends BaseTest {

    @Test
    public void clickStartAndOpenCmd() {
        getClient().click(new ImageBox(getResourcePath(RESOURCE_BUTTON_IMAGE), SIMILARITY), WAIT_TIMEOUT);
        getClient().setText(new ImageBox(getResourcePath(RESOURCE_INPUT_IMAGE), SIMILARITY), "cmd" + Key.ENTER, WAIT_TIMEOUT);
        assertTrue(getClient().exists(new ImageBox(getResourcePath(RESOURCE_LABEL_IMAGE), SIMILARITY), WAIT_TIMEOUT));
    }
}
