package cop5556sp17;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class Test {
	
	static URL test;
	static URL test2;
	
	static File f;
	
	public Test(String[] args) throws Exception{
		PLPRuntimeImageIO imageIO = new PLPRuntimeImageIO();
		test = imageIO.getURL(args, 1);
		BufferedImage img = imageIO.readFromURL(test);
		BufferedImage img2 = imageIO.readFromURL(test2);
		
		PLPRuntimeImageIO.write(img, f);
		
		PLPRuntimeImageOps.mod(img, 2);
		
		
		
		img = img2;
		
		
//		PLPRuntimeFrame.createOrSetFrame(img, null).getScreenHeight();
		
		
	}

}
