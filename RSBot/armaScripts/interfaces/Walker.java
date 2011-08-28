package armaScripts.interfaces;

import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSTile;

public interface Walker {
	
	public RSTile getNext();
	
	public boolean walkTo(RSTile tile);
	
	public boolean walkTo(RSTile tile, Checker conditionMet);
	
	public boolean walkTo(RSCharacter character);
	
	public boolean walkTo(RSCharacter character, Checker conditionMet);
	
	public boolean walkTo(RSArea area);
	
	public boolean walkTo(RSArea area, Checker conditionMet);
	
	public boolean walkPath(RSPath path);
	
	public boolean walkPath(RSPath path, Checker c);
	
	public RSPath getPath();
	
	public void setPath(RSPath path);
	
	public long getTimerLength();
	
	public void setTimerLength(long timerLength);
	
	public int getXAccuracy();
	
	public int getYAccuracy();
	
	public void setAccuracy(int x, int y);

}
