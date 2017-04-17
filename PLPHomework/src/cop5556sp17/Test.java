package cop5556sp17;

import java.io.File;
import java.net.URL;

public class Test {
	
	public Test(String[] args) throws Exception{
		PLPRuntimeImageIO imageIO = new PLPRuntimeImageIO();
		URL test = imageIO.getURL(args, 1);
	}

}
