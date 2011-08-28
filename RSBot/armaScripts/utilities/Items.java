package armaScripts.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import org.rsbot.Configuration;

public class Items {

	private static final String[] ITEM_NAMES;
	private static final int[][] ITEM_IDS;
	static {
		final Logger logger = Logger.getLogger("ArmaScripts");
		final File dir = new File(Configuration.Paths.getScriptCacheDirectory(), "ArmaScripts");
		if (!dir.exists())
			dir.mkdirs();
		final File file = new File(dir, "Item Names.txt");
		if(!file.exists()){
			try{
				file.createNewFile();
				logger.info("Downloading Item Names.txt");
				final BufferedWriter out = new BufferedWriter(new FileWriter(file));
				final URL url = new URL("https://raw.github.com/Armanious/RSBot/master/Item%20Names.txt");
				final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				String s;
				while((s = in.readLine()) != null){
					out.write(s);
					out.newLine();
				}
				in.close();
				out.close();
				logger.info("Succussfully downloaded Item Names.txt");
			}catch(Exception e){
				e.printStackTrace();
			}				
		}
		int length = 0;
		BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader(file));
			length = Integer.parseInt(in.readLine().substring(3));
		}catch(Exception e){
			e.printStackTrace();
		}
		ITEM_NAMES = new String[length]; 
		ITEM_IDS = new int[length][];
		//All these ugly try-catch blocks are so that this gets intialized and I can keep my final in the declaration
		try{
			if(length > 0 && in != null){
				for(int i = 0; i < length; i++){
					String s = in.readLine();
					int index = s.indexOf('-');
					ITEM_NAMES[i] = s.substring(index + 1);
					if(s.indexOf(',') == -1){
						ITEM_IDS[i] = new int[]{Integer.parseInt(s.substring(0, index))};
					}else{
						String[] stringIds = s.substring(0, index).split(",");
						int[] ids = new int[stringIds.length];
						for(int j = 0; j < ids.length; j++){
							if(stringIds[j].isEmpty())
								System.out.println(s);
							ids[j] = Integer.parseInt(stringIds[j]);
						}
						ITEM_IDS[i] = ids;
					}
				}
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static int[] getItemIDsExactString(String s){
		int index = Arrays.binarySearch(ITEM_NAMES, s);
		if(index < 0)
			return new int[]{-1};
		return ITEM_IDS[index];
	}
	
	public static int[] getItemIDs(String startWith){
		startWith = startWith.toLowerCase();
		final HashSet<Integer> intSet = new HashSet<Integer>();
		for(int i = 0; i < ITEM_NAMES.length; i++){
			if(ITEM_NAMES[i].toLowerCase().contains(startWith)){
				for(int id : ITEM_IDS[i]){
					intSet.add(id);
				}
			}
		}
		if(intSet.isEmpty())
			intSet.add(-1);
		int[] ints = new int[intSet.size()];
		final Iterator<Integer> iter = intSet.iterator();
		for(int i = 0; i < ints.length; i++)
			ints[i] = iter.next();
		return ints;
	}

	public static int getItemID(String s){
		return getItemIDs(s)[0];
	}
	
	public static String getItemName(int id){
		for(int i = 0; i < ITEM_NAMES.length; i++){
			if(Arrays.binarySearch(ITEM_IDS[i], id) >= 0){
				return ITEM_NAMES[i];
			}
		}
		return "null";
	}
	
	public static String[] getItemNames(){
		String[] a = new String[ITEM_NAMES.length];
		System.arraycopy(ITEM_NAMES, 0, a, 0, ITEM_NAMES.length);
		return a;
	}
	
}
