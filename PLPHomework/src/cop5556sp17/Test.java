package cop5556sp17;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class Test {
	
	static URL test;
	static File fn;
	
	public Test(String[] args) throws Exception{
		PLPRuntimeImageIO imageIO = new PLPRuntimeImageIO();
		test = imageIO.getURL(args, 1);
		BufferedImage img = imageIO.readFromURL(test);
		PLPRuntimeFrame.createOrSetFrame(img, null).showImage().moveFrame(1, 2).getYVal();
		
		
	}

}
