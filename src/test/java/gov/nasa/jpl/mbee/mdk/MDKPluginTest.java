package gov.nasa.jpl.mbee.mdk;

import com.nomagic.magicdraw.plugins.PluginUtils;
import gov.nasa.jpl.mbee.mdk.test.framework.ApplicationStartClassRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationStartClassRunner.class)
public class MDKPluginTest {

    private static MDKPlugin plugin;

    @BeforeClass
    public static void setup() {
        plugin = (MDKPlugin) PluginUtils.getPlugins().stream().filter(s -> s instanceof MDKPlugin).findFirst().get();
    }

    @Test
    public void init() {
        assertTrue("MDK shall be installed in MagicDraw.", plugin != null && plugin.getDescriptor().getName().equals("Model Development Kit"));
    }

    @Test
    public void getOclPlugin() {
        assertTrue("Ocl Evaluator plugin shall be installed in MagicDraw via the MDK Plugin.", plugin != null && plugin.getOclPlugin() != null);
    }

    @Test
    public void getVcPlugin() {
        assertTrue("ValidateConstraint Evaluator plugin shall be installed in MagicDraw via the MDK Plugin.", plugin != null && plugin.getVcPlugin() != null);
    }

}