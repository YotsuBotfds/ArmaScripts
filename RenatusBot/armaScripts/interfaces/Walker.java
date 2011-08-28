package armaScripts.interfaces;

import org.rsbot.script.wrappers.Area;
import org.rsbot.script.wrappers.Character;
import org.rsbot.script.wrappers.Path;
import org.rsbot.script.wrappers.Tile;

public interface Walker {
	
	public Tile getNext();
	
	public boolean walkTo(Tile tile);
	
	public boolean walkTo(Tile tile, Checker conditionMet);
	
	public boolean walkTo(Character character);
	
	public boolean walkTo(Character character, Checker conditionMet);
	
	public boolean walkTo(Area area);
	
	public boolean walkTo(Area area, Checker conditionMet);
	
	public boolean walkPath(Path path);
	
	public boolean walkPath(Path path, Checker c);
	
	public Path getPath();
	
	public void setPath(Path path);
	
	public long getTimerLength();
	
	public void setTimerLength(long timerLength);
	
	public int getXAccuracy();
	
	public int getYAccuracy();
	
	public void setAccuracy(int x, int y);

}
