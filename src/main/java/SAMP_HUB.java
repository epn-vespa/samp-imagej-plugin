/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */



import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.astrogrid.samp.client.*;
import org.astrogrid.samp.*;
import org.astrogrid.samp.hub.Hub;
import org.astrogrid.samp.hub.HubServiceMode;
import org.padc.*;

/**
 * A template for processing each pixel of either
 * GRAY8, GRAY16, GRAY32 or COLOR_RGB images.
 *
 * @author Johannes Schindelin
 */
public class SAMP_HUB implements PlugIn {
    static HubConnector conn = null;
    
    public void runHub(ClientProfile profile){                
        if (!profile.isHubRunning()){
            try {
                System.out.println("start hub");
                Hub.runHub(HubServiceMode.MESSAGE_GUI);
            } catch (IOException ex) {
                Logger.getLogger(SAMP_HUB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }
    
    public String getUrl(String uri){
        URL url;
        String contentType =null;
        
        if ( uri.contains("http://") || uri.contains("https://")){
            try {
                url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();                
                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER){
                    // get redirect url from "location" header field
                    String newUrl = connection.getHeaderField("Location");                                
                    // open the new connnection again
                    System.out.println("Redirect to URL : " + newUrl);                    
                    return getUrl(newUrl);
                }                
                
            } 
            catch (MalformedURLException ex) {
                System.out.println("Invalid "+uri);
                ex.printStackTrace();
            } 
            catch (IOException ex) {
                System.out.println("Cant open "+uri);
                ex.printStackTrace();
            }
        }
        return uri;
    }
    
    /**
     * @param uri
     * @return
     */
    public String getType(String uri){
        URL url;
        String contentType =null;
        
        if ( uri.contains("http://") || uri.contains("https://")){
            try {
                System.out.println("uri is url "+uri);
                url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();                
                int status = connection.getResponseCode();
                if(status == 404){
                    IJ.showMessage("SAMP HUB",
                    "Url:"+uri+" not found");
                }
                else{
                    contentType = connection.getContentType();
                    if ( contentType == null){
                        System.out.println("WARNING : no content type for url "+uri);
                        if ( uri.endsWith(".fits")){
                            contentType="image/fits";
                        }
                    }
                }
                
            } 
            catch (MalformedURLException ex) {
                System.out.println("Invalid "+uri);
                ex.printStackTrace();
            } 
            catch (IOException ex) {
                System.out.println("Cant open "+uri);
                ex.printStackTrace();
            }
        }
        return contentType;
    }
            
    @Override
    public void run(String args){              
        //showAbout();
        ClientProfile profile = DefaultClientProfile.getProfile();
        
        runHub(profile);
        if (conn == null){            
            conn = new HubConnector(profile);
        }        
        if (!conn.isConnected()){
        
            Metadata meta = new Metadata();
            meta.setName("IMAGE-J");
            meta.setDescriptionText("Image J");
            conn.declareMetadata(meta);
            conn.addMessageHandler(new AbstractMessageHandler("image.load.fits") {
                public Map processCall(HubConnection c, String senderId, Message msg) {
                    // 
                    Map params = msg.getParams();
                    String msg_url = params.get("url").toString();
                    String url = getUrl(msg_url);
                    String type = getType(url);
                        
                    System.out.println("type |"+type+"|");                    
                    if ( url.startsWith("file://localhost", 0) || (type != null && "image/fits".equals(type))){
                            String[] parts = url.split("/");
                            ImagePlus image;
                            try {
                                String name = parts[parts.length-1];                                     
                                int index=url.lastIndexOf('/');
                                String path = url.substring(0,index)+"/";
                                image = (ImagePlus)new FitsReader(path,parts[parts.length-1]);
                                image.show();                        
                            } catch (IOException ex) {
                                
                                //Logger.getLogger(SAMP_HUB.class.getName()).log(Level.SEVERE, null, ex);
                                System.out.println("try to open url "+url);
                                IJ.debugMode=true;
                                image = IJ.openImage(url);
                                image.show();
                            }
                    }
                    else{
                        System.out.println("try to open url");
                        IJ.debugMode=true;
                        ImagePlus image = IJ.openImage(url);
                        image.show();
                    }                    
                    return null;// do stuff
                }
            });
            Subscriptions subs = conn.computeSubscriptions();
            subs.addMType("image.load.fits");           
            conn.declareSubscriptions(subs);   
            try {
                conn.getConnection();
            } catch (SampException ex) {
                Logger.getLogger(SAMP_HUB.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }        
    }

    public void showAbout() {
            IJ.showMessage("SAMP HUB",
                "hub to use SAMP protocol"
            );
    }

    /**
     * Main method for debugging.
     *
     * For debugging, it is convenient to have a method that starts ImageJ, loads
     * an image and calls the plugin, e.g. after setting breakpoints.
     *
     * @param args unused
     */
    
    public static final void testUrl(String url) throws IOException{
        int index = url.lastIndexOf('/');
        String path = url.substring(0,index)+"/";
        String name = url.substring(index+1);
        System.out.println("path "+path+" "+name);
        ImagePlus image = new FitsReader(path, name);
        image.show();
    }
    
    public static void main(String[] args) throws IOException {
            // set the plugins.dir property to make the plugin appear in the Plugins menu
            Class<?> clazz = SAMP_HUB.class;
            String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
            String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
            System.setProperty("plugins.dir", pluginsDir);

            // start ImageJ
            new ImageJ();

        testUrl("http://voparis-srv.obspm.fr/vo/planeto/apis/dataset/Bastet/Uranus_-_2011_09-29_Nov/obrx18hbq_x2d.fits");
        
        //image = (ImagePlus)new FitsReader("file://localhost/home/haigron/.aladin/.aladin.33916/","sampBj%28IIIa-J.GG395%29.ivo%3A..vopdc.obspm1500650771926.fits");
        //ImagePlus image = new FitsReader("http://climso.irap.omp.eu/data/files/epntap/","imoa_03933_l2_20170727_16212700_emi1.fts.bz2");           
        //    image.show();
            
            // run the plugin
            //IJ.runPlugIn(clazz.getName(), "");
    }
}
