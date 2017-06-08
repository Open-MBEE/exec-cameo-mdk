package gov.nasa.jpl.mbee.mdk.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Print an image with optional caption. If you're using this directly, you will
 * need to add the documentation as the caption if applicable, and set the
 * title.
 *
 * @author dlam
 */
public class DBImage extends DocumentElement {

    private Diagram image;
    private String caption;
    private boolean gennew;
    private boolean doNotShow;
    private boolean isTomSawyerImage;



    private String outputDir;
    private String imageFileName;

    public DBImage(Diagram d) {
        image = d;
        gennew = false;
        isTomSawyerImage = false;
    }

    public DBImage() {
        gennew = false;
    }

    public void setDiagram(Diagram d) {
        image = d;
    }

    public void setCaption(String cap) {
        caption = cap;
    }

    public void setGennew(boolean b) {
        gennew = b;
    }

    public Diagram getImage() {
        return image;
    }

    public void setImage(Diagram image) {
        this.image = image;
    }

    public String getCaption() {
        return caption;
    }

    public boolean isGennew() {
        return gennew;
    }

    public boolean isDoNotShow() {
        return doNotShow;
    }

    public void setDoNotShow(boolean b) {
        doNotShow = b;
    }

    public boolean isTomSawyerImage(){return isTomSawyerImage;}

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        int pos = sb.lastIndexOf(")");
        sb.insert(pos, ", " + getImage());
        return sb.toString();
    }

    public void setIsTomSawyerImage(boolean isTomSawyerImage) {
        this.isTomSawyerImage = isTomSawyerImage;
    }

    public List<String> getTSImageInfo() {
        List<String> tsimageInfo = new ArrayList<>();
        File svgdiagramFile = new File(outputDir, imageFileName);

        tsimageInfo.add(imageFileName);
        String scale = "true";
        try {
            BufferedReader svg = new BufferedReader(new FileReader(svgdiagramFile));
            String line = svg.readLine();
            while (line != null) {
                if (line.startsWith("<svg")) {
                    int widthindex = line.indexOf("width=\"");
                    if (widthindex > -1) {
                        int endindex = line.indexOf("\"", widthindex + 7);
                        String w = line.substring(widthindex + 7, endindex);
                        double wd = Double.parseDouble(w);
                        if (wd < 5.5) {
                            scale = "false";
                        }
                    }
                    break;
                }
                line = svg.readLine();
            }
            svg.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        tsimageInfo.add(scale);
        return tsimageInfo;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
}
