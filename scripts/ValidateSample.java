import com.dot.floatingpet.core.SpriteAtlas;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/** Decodes the actual file and uses the app's production atlas API; not an Android UI test. */
public final class ValidateSample {
    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        if (file.length() > 12L*1024*1024) throw new AssertionError("Image exceeds import size limit");
        BufferedImage image = ImageIO.read(file);
        if (image == null || !image.getColorModel().hasAlpha()) throw new AssertionError("Transparent PNG required");
        SpriteAtlas.Layout layout = SpriteAtlas.forDimensions(image.getWidth(),image.getHeight());
        int frames=0;
        for (int row=0;row<layout.rowCount();row++) {
            for (int col=0;col<8;col++) {
                if (col<layout.frameCount(row)) {
                    int[] r=layout.frameRect(row,col);
                    int occupied=0;
                    for(int y=r[1];y<r[3];y++) for(int x=r[0];x<r[2];x++) {
                        int alpha=image.getRGB(x,y)>>>24;
                        if(alpha>0) occupied++;
                        if(alpha>0 && (x-r[0]<4 || r[2]-x<=4 || y-r[1]<4 || r[3]-y<=4)) throw new AssertionError("Cell margin violated");
                    }
                    if(occupied<1000) throw new AssertionError("Sparse frame");
                    frames++;
                } else {
                    for(int y=row*208;y<(row+1)*208;y++) for(int x=col*192;x<(col+1)*192;x++)
                        if((image.getRGB(x,y)>>>24)!=0) throw new AssertionError("Nontransparent padding");
                }
            }
        }
        if(frames!=57) throw new AssertionError("Expected 57 frames");
        System.out.println("PASS: actual PNG decoded, production SpriteAtlas accepted exact v1 dimensions, 57 safe occupied frame crops and 15 fully transparent padding cells");
    }
}
