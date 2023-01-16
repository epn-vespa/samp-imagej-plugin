

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author haigron
 */
public class SampHubJUnitTest {
    SAMP_HUB hub;
    
    public SampHubJUnitTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        hub = new SAMP_HUB();
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testNullType() {
        Assert.assertNull("type must be null",hub.getType("file://test"));
    }
    
    @Test
    public void testHttpNoContentType() {        
        Assert.assertEquals("http fits", "image/fits", 
                hub.getType("https://ws.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/raven/files/mast:HST/product/x2qh0102t_c1f.fits"));
    }
        
}
