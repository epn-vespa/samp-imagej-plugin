/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.padc;

import ij.ImagePlus;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author haigron
 */
public class FitsReaderJUnitTest {

    public FitsReaderJUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:

    @Test
    public void testFitsReader() throws IOException {
        ImagePlus f = new FitsReader(
                "https://idoc-medoc.ias.u-psud.fr/sitools/datastorage/user/archive_SDO_DEM/2023/10/DEM_aia_2023-10-03T23_35_images/",
                "DEM_aia_2023-10-03T23_35_width.fits");
        Assert.assertNotNull("image plus not null", f);
    }
    /*
     * 
     * @Test
     * public void testFitsReaderBz2() throws IOException {
     * ImagePlus f = new FitsReader("http://climso.irap.omp.eu/data/files/epntap/",
     * "imoa_03933_l2_20170727_16212700_emi1.fts.bz2");
     * Assert.assertNotNull("image plus not null", f);
     * }
     */

}
