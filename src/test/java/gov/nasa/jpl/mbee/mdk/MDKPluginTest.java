package gov.nasa.jpl.mbee.mdk;

import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.plugins.PluginUtils;
import gov.nasa.jpl.mbee.mdk.test.framework.ApplicationStartClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationStartClassRunner.class)
public class MDKPluginTest {

    @Test
    public void init() {
        assertTrue("MDK shall be installed in MagicDraw.", PluginUtils.getPlugins().stream().map(Plugin::getDescriptor).anyMatch(descriptor -> descriptor.getName().equals("Model Development Kit")));
    }
}