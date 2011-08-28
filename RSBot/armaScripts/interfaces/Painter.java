package armaScripts.interfaces;

import java.awt.Color;
import java.awt.Graphics;

import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

public interface Painter {
	
	public boolean isPaintingPath();
	
	public void setPaintingPath(boolean paintingPath);
	
	public void drawTile(Graphics g, RSTile tile);
	
	public void drawTile(Graphics g, RSTile tile, Color c);
	
	public void drawTileOnMap(Graphics g, RSTile tile);
	
	public void drawTileOnMap(Graphics g, RSTile tile, Color c);
	
	public void drawAreaOnMap(Graphics g, RSArea area);
	
	public void drawAreaOnMap(Graphics g, RSArea area, Color c);
	
	public void drawObject(Graphics g, RSObject obj);
	
	public void drawObject(Graphics g, RSObject obj, Color c);
	
	public void drawCharacter(Graphics g, RSCharacter character);
	
	public void drawCharacter(Graphics g, RSCharacter character, Color c);
	
	public void drawGroundItem(Graphics g, RSGroundItem item);

	public void drawGroundItem(Graphics g, RSGroundItem item, Color c);
	
	public void paint(Graphics g);

}
