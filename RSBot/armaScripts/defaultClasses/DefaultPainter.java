package armaScripts.defaultClasses;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

import org.rsbot.script.Script;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

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
	public void drawTile(Graphics g, RSTile tile) {
		drawTile(g, tile, Color.magenta);
	}

	@Override
	public void drawTile(Graphics g, RSTile tile, Color c) {
		final Point p = script.ctx.calc.tileToScreen(tile);
		if(p.x == -1 || p.y == -1)
			return;
		g.setColor(c);
		g.fillRect(p.x - 8, p.y - 8, 16, 16);
		g.setColor(Color.black);
		g.drawRect(p.x - 8, p.y - 8, 16, 16);
	}

	@Override
	public void drawTileOnMap(Graphics g, RSTile tile) {
		drawTileOnMap(g, tile, Color.magenta);
	}

	@Override
	public void drawTileOnMap(Graphics g, RSTile tile, Color c) {
		final Point p = script.ctx.calc.tileToMinimap(tile);
		if(p.x == -1 || p.y == -1)
			return;
		g.setColor(c);
		g.fillRect(p.x - 2, p.y - 2, 4, 4);
	}

	@Override
	public void drawAreaOnMap(Graphics g, RSArea area) {
		drawAreaOnMap(g, area, new Color(0, 0, 200, 175));

	}

	@Override
	public void drawAreaOnMap(Graphics g, RSArea area, Color c) {
		for(RSTile tile : area.getTileArray()){
			drawTileOnMap(g, tile, c);
		}
	}

	private void drawModel(Graphics g, RSModel model, Color c){
		if(model == null)
			return;
		g.setColor(c);
		for(Polygon triangle : model.getTriangles())
			g.drawPolygon(triangle);
	}

	@Override
	public void drawObject(Graphics g, RSObject obj) {
		drawObject(g, obj, Color.red);
	}

	@Override
	public void drawObject(Graphics g, RSObject obj, Color c) {
		drawModel(g, obj.getModel(), c);
	}

	@Override
	public void drawCharacter(Graphics g, RSCharacter character) {
		drawCharacter(g, character, Color.orange);
	}

	@Override
	public void drawCharacter(Graphics g, RSCharacter character, Color c) {
		drawModel(g, character.getModel(), c);
	}

	@Override
	public void drawGroundItem(Graphics g, RSGroundItem item) {
		drawGroundItem(g, item, Color.blue);
	}

	@Override
	public void drawGroundItem(Graphics g, RSGroundItem item, Color c) {
		drawModel(g, item.getModel(), c);
	}
	
	public void paint(Graphics g) {
		if(paintPath){
			final RSTile next = DefaultWalker.get(script).getNext();
			if(next != null){
				final RSTile loc = script.ctx.players.getMyPlayer().getLocation();
				final Point p1 = script.ctx.calc.tileToMinimap(loc);
				final Point p2 = script.ctx.calc.tileToMinimap(next);
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
