/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.padc;

import java.io.*;
import ij.*;
import ij.io.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.FITS_Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
/**
 *
 * @author haigron
 */
public class FitsReader extends ImagePlus{

    final void process(MultiFitsDecoder mf){
        List<FileInfo> fiList = mf.fiList;
        String name = mf.name;
        FileInfo first = fiList.get(0);
        
        ImageStack stack = new ImageStack(first.width,first.height);
        int i=0;
        
        for (FileInfo fi:fiList){            
            FileOpener fo = new FileOpener(fi);
            ImagePlus imp = fo.open(false);

            ImageProcessor ip1 = imp.getProcessor();
            ip1.flipVertical();
            setProcessor(name, ip1);
            Calibration cal = imp.getCalibration();
            if (fi.fileType == FileInfo.GRAY16_SIGNED && mf.bscale == 1.0 && mf.bzero == 32768.0) {
                cal.setFunction(Calibration.NONE, null, "Gray Value");
            }
            setCalibration(cal);
            setFileInfo(fi);
            i++;
            stack.addSlice(name + "_" + i, ip1.getPixels());
        }
        setStack(name, stack);            
            
            //setProperty("Info", fd.getHeaderInfo());
        setFileInfo(first); // needed for File->Revert
            //IJ.showStatus("");*/
    }        
    
    public FitsReader(String url, String name) throws IOException {
        try {        
                            
            Fits f = new Fits(url+name);                     
            
            f.read();            
            
            int nbHdu = f.getNumberOfHDUs();
            System.out.println("nbHdus "+nbHdu);
            long offset = 0;
            long size = 0;            
            MultiFitsDecoder fd = new MultiFitsDecoder(url,name);
            
            for(int i=0; i< nbHdu; i++){                
                BasicHDU hdu = f.getHDU(i);   
                Header h = hdu.getHeader();                              
                long psize = h.getNumberOfPhysicalCards();                               
                System.out.println("card+end"+psize);
                
                offset += 2880+2880*(((psize*80)-1)/2880);                                
                int naxis = h.getIntValue("NAXIS");                
                String type = h.getStringValue("XTENSION");
                System.out.println("type "+type);
                if ( naxis == 2 && (type == null || type.equals("IMAGE"))){ 
                    System.out.println("add info: hdu"+i);                                                                                                                                           
                    fd.addInfos(h,offset);                                        
                }                
                Data data = hdu.getData();
                long dsize = data.getSize();                
                offset +=dsize;
            }
            process(fd);
        } catch (FitsException ex) {
            Logger.getLogger(FitsReader.class.getName()).log(Level.SEVERE, null, ex);
        }/*
        FitsDecoder fd = new FitsDecoder(url,name);
        FileInfo fi;        
        fi = fd.getInfo();
        
        System.out.println(fi.width+" "+fi.height+" "+fi.offset);
        if (fi!=null && fi.width>0 && fi.height>0 && fi.offset>0) {
            FileOpener fo = new FileOpener(fi);
            ImagePlus imp = fo.openImage();
            if(fi.nImages==1) {
              ImageProcessor ip = imp.getProcessor();              
              ip.flipVertical(); // origin is at bottom left corner
              setProcessor(name, ip);
            } else {
              ImageStack stack = imp.getStack(); // origin is at bottom left corner              
              for(int i=1; i<=stack.getSize(); i++)
                  stack.getProcessor(i).flipVertical();
              setStack(name, stack);
            }
            Calibration cal = imp.getCalibration();
            if (fi.fileType==FileInfo.GRAY16_SIGNED && fd.bscale==1.0 && fd.bzero==32768.0)
                cal.setFunction(Calibration.NONE, null, "Gray Value");
            setCalibration(cal);
            setProperty("Info", fd.getHeaderInfo());
            setFileInfo(fi); // needed for File->Revert            
        } else
            IJ.error("This does not appear to be a FITS file.");
        //IJ.showStatus("");*/
    }

}



class MultiFitsDecoder {
    List<FileInfo> fiList = new ArrayList();    
    private final StringBuffer info = new StringBuffer(10000);
    double bscale, bzero;
    String url, name;
    public MultiFitsDecoder(String url, String name) {
        this.name = name;
        this.url = url;        
    }

    public void setBitPix(FileInfo fi, int bitpix){
        switch (bitpix) {
            case 8:
                fi.fileType = FileInfo.GRAY8;
                break;
            case 16:
                fi.fileType = FileInfo.GRAY16_SIGNED;
                break;
            case 32:
                fi.fileType = FileInfo.GRAY32_INT;
                break;
            case -32:
                fi.fileType = FileInfo.GRAY32_FLOAT;                
                break;
            case -64:
                fi.fileType = FileInfo.GRAY64_FLOAT;
                break;
            default:
                IJ.error("BITPIX must be 8, 16, 32, -32 (float) or -64 (double).");                                
        }
    }
    
    public void setOffset(FileInfo fi, long offset){
        fi.longOffset = offset;
    }
    public void addInfos(Header head, long offset) throws IOException {
        FileInfo fi = new FileInfo();
        fi.fileFormat = FileInfo.FITS;
        fi.fileName = name;
        fi.url = url;
        fi.width = 0;
        fi.height = 0;
        fi.offset = 0;        
        setOffset(fi, offset);
        int count = head.getNumberOfPhysicalCards();        
        int bitpix = head.getIntValue("BITPIX");
        
        setBitPix(fi,bitpix);
        
        int naxis1 = head.getIntValue("NAXIS1");
        int naxis2 = head.getIntValue("NAXIS2");
        //int naxis3 = head.getIntValue("NAXIS3");
        fi.width = naxis1;
        fi.height = naxis2;
        fi.nImages = 1;
        
        bscale = head.getDoubleValue("BSCALE");
        bzero = head.getDoubleValue("BZERO");
        
        fi.pixelWidth = head.getDoubleValue("CDELT1");
        fi.pixelHeight = head.getDoubleValue("CDELT2");
        fi.pixelDepth = head.getDoubleValue("CDELT3");
        
        fi.unit = head.getStringValue("CTYPE1");
        
        if (fi.width > 0 && fi.height > 0 && fi.longOffset > 0) { 
            fiList.add(fi);        
        }
    }            

}

