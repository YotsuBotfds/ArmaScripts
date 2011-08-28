package armaScripts.defaultClasses;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

import org.rsbot.script.Script;
import org.rsbot.script.methods.Calculations;
import org.rsbot.script.methods.Players;
import org.rsbot.script.wrappers.Area;
import org.rsbot.script.wrappers.GameModel;
import org.rsbot.script.wrappers.GameObject;
import org.rsbot.script.wrappers.GroundItem;
import org.rsbot.script.wrappers.Targetable;
import org.rsbot.script.wrappers.Tile;

import armaScripts.interfaces.Painter;

public class DefaultPainter implements Painter {

	private final Script script;
	private boolean paintPath = true;

	public DefaultPainter(Script script){
		this.script = script;
	}

	@Override
	public boolean isPaintingPath(){
		return paintPath;
	}

	@Override
	public void setPaintingPath(boolean paintingPath){
		paintPath = paintingPath;
	}

	@Override
	public void drawTile(Graphics g, Tile tile) {
		drawTile(g, tile, Color.magenta);
	}

	@Override
	public void drawTile(Graphics g, Tile tile, Color c) {
		final Point p = Calculations.worldToScreen(tile.getX(), tile.getY(), tile.getZ());
		if(p.x == -1 || p.y == -1)
			return;
		g.setColor(c);
		g.fillRect(p.x - 8, p.y - 8, 16, 16);
		g.setColor(Color.black);
		g.drawRect(p.x - 8, p.y - 8, 16, 16);
	}

	@Override
	public void drawTileOnMap(Graphics g, Tile tile) {
		drawTileOnMap(g, tile, Color.magenta);
	}

	@Override
	public void drawTileOnMap(Graphics g, Tile tile, Color c) {
		final Point p = Calculations.worldToMinimap(tile.getX(), tile.getY());
		if(p.x == -1 || p.y == -1)
			return;
		g.setColor(c);
		g.fillRect(p.x - 2, p.y - 2, 4, 4);
	}

	@Override
	public void drawAreaOnMap(Graphics g, Area area) {
		drawAreaOnMap(g, area, new Color(0, 0, 200, 175));

	}

	@Override
	public void drawAreaOnMap(Graphics g, Area area, Color c) {
		for(Tile tile : area.getTileArray()){
			drawTileOnMap(g, tile, c);
		}
	}

	private void drawModel(Graphics g, GameModel model, Color c){
		if(model == null)
			return;
		g.setColor(c);
		for(Polygon triangle : model.getTriangles())
			g.drawPolygon(triangle);
	}

	@Override
	public void drawObject(Graphics g, GameObject obj) {
		drawObject(g, obj, Color.red);
	}

	@Override
	public void drawObject(Graphics g, GameObject obj, Color c) {
		drawModel(g, obj.getModel(), c);
	}

	@Override
	public void drawTargetable(Graphics g, Targetable targetable) {
		drawTargetable(g, targetable, Color.orange);
	}

	@Override
	public void drawTargetable(Graphics g, Targetable targetable, Color c) {
		g.setColor(c);
		targetable.draw(g);
	}

	@Override
	public void drawGroundItem(Graphics g, GroundItem item) {
		drawGroundItem(g, item, Color.blue);
	}

	@Override
	public void drawGroundItem(Graphics g, GroundItem item, Color c) {
		drawModel(g, item.getModel(), c);
	}
	
	public void paint(Graphics g) {
		if(paintPath){
			final Tile next = DefaultWalker.get(script).getNext();
			if(next != null){
				final Tile loc = Players.getLocal().getLocation();
				final Point p1 = Calculations.worldToMinimap(loc.getX(), loc.getY());
				final Point p2 = Calculations.worldToMinimap(next.getX(), next.getY());
				if(p2.x != -1 && p2.y != -1){
					g.setColor(Color.green);
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
					g.setColor(Color.magenta);
					g.fillRect(p1.x - 2, p1.y - 2, 4, 4);
					g.fillRect(p2.x - 2, p2.y - 2, 4, 4);
					g.setColor(Color.black);
					g.drawRect(p1.x - 2, p1.y - 2, 4, 4);
					g.drawRect(p2.x - 2, p2.y - 2, 4, 4);
				}
			}
		}
	}

}
