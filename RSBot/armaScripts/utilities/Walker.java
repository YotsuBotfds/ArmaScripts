package armaScripts.utilities;

import java.util.EnumSet;

import org.rsbot.script.Script;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSPath.TraversalOption;
import org.rsbot.script.wrappers.RSTile;

import armaScripts.interfaces.Checker;

public class Walker {
	
	private final Script script;
	
	public RSPath path;
	
	public Walker(Script script){
		this.script = script;
	}
	
	public boolean walkToArea(final RSArea area){
		return walkToArea(area, new Checker(){
			public boolean goodToContinue(){
				return !area.contains(script.ctx.players.getMyPlayer().getLocation());
			}
		});
	}

	public boolean walkToArea(RSArea area, Checker checker){
		if(walkToTile(area.getCentralTile(), checker))
			return area.contains(script.ctx.players.getMyPlayer().getLocation());
		return false;
	}

	public boolean walkToCharacter(final RSCharacter c){
		return walkToCharacter(c, new Checker(){
			public boolean goodToContinue(){
				return !c.isOnScreen();
			}
		});
	}

	public boolean walkToCharacter(RSCharacter c, Checker checker){
		return walkToTile(c.getLocation(), checker);
	}

	public boolean walkToTile(RSTile tile){
		return walkToTile(tile, null);
	}

	public boolean walkToTile(RSTile tile, Checker checker){
		return walkPath(script.ctx.walking.getPath(tile), checker);
	}

	public boolean walkPath(RSPath path){
		return walkPath(path, null);
	}

	public boolean walkPath(RSPath path, Checker checker){
		this.path = path;
		long lastMoved = 0;
		long startTime = System.currentTimeMillis();
		while(path != null && lastMoved <= 15000 && script.isActive() && (checker == null || checker.goodToContinue())){
			final RSTile dest = script.ctx.walking.getDestination();
			if(dest != null && script.ctx.calc.distanceBetween(dest, path.getEnd()) <= 3)
				return true;
			if(dest == null || script.ctx.calc.distanceTo(dest) <= 8){
				if(path.getNext() == null){
					path = script.ctx.walking.getPath(path.getEnd());
					continue;
				}
				if(!path.traverse(EnumSet.of(TraversalOption.HANDLE_RUN, TraversalOption.SPACE_ACTIONS))){
					if(script.ctx.calc.distanceTo(path.getEnd()) <= 6)
						return true;
				}else{
					startTime = System.currentTimeMillis();
				}
			}
			lastMoved = System.currentTimeMillis() - startTime;
		}
		if(path == null)
			return !checker.goodToContinue();
		return script.ctx.calc.distanceTo(path.getEnd()) <= 4 || !checker.goodToContinue();
	}

}
