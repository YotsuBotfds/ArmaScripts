package armaScripts.utilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSTile;

public class Painter {
	
	public Graphics g;
	
	private final MethodContext ctx;
	
	public Painter(MethodContext ctx){
		this.ctx = ctx;
	}
	
	public void paintTileOnMap(RSTile t, Color c){
		final Point p = ctx.calc.tileToMinimap(t);
		if(p.x == -1 || p.y == -1)
			return;
		g.setColor(c);
		g.fillRect(p.x - 2, p.y - 2, 4, 4);
		g.setColor(new Color(0, 0, 0, c.getTransparency()));
		g.drawRect(p.x - 2, p.y - 2, 4, 4);
	}

}
