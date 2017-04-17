package cop5556sp17;

import java.io.File;
import java.net.URL;

public class Test {
	
	static URL test;
	static File fn;
	
	public Test(String[] args) throws Exception{
		PLPRuntimeImageIO imageIO = new PLPRuntimeImageIO();
//		test = imageIO.getURL(args, 1);
//		imageIO.readFromURL(test);
		
		imageIO.readFromFile(fn);
		
		
	}

}
