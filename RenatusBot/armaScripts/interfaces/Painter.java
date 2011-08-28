package armaScripts.interfaces;

import java.awt.Color;
import java.awt.Graphics;

import org.rsbot.script.wrappers.Area;
import org.rsbot.script.wrappers.GameObject;
import org.rsbot.script.wrappers.GroundItem;
import org.rsbot.script.wrappers.Targetable;
import org.rsbot.script.wrappers.Tile;

public interface Painter {
	
	public boolean isPaintingPath();
	
	public void setPaintingPath(boolean paintingPath);
	
	public void drawTile(Graphics g, Tile tile);
	
	public void drawTile(Graphics g, Tile tile, Color c);
	
	public void drawTileOnMap(Graphics g, Tile tile);
	
	public void drawTileOnMap(Graphics g, Tile tile, Color c);
	
	public void drawAreaOnMap(Graphics g, Area area);
	
	public void drawAreaOnMap(Graphics g, Area area, Color c);
	
	public void drawObject(Graphics g, GameObject obj);
	
	public void drawObject(Graphics g, GameObject obj, Color c);
	
	public void drawTargetable(Graphics g, Targetable targetable);
	
	public void drawTargetable(Graphics g, Targetable targetable, Color c);
	
	public void drawGroundItem(Graphics g, GroundItem item);

	public void drawGroundItem(Graphics g, GroundItem item, Color c);
	
	public void paint(Graphics g);

}
