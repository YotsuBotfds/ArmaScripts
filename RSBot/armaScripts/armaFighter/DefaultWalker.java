package armaScripts.armaFighter;

import java.awt.Color;
import java.util.HashMap;

import org.rsbot.script.Script;
import org.rsbot.script.methods.Methods;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSLocalPath;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSTile;

import armaScripts.interfaces.Checker;
import armaScripts.interfaces.Walker;

public class DefaultWalker implements Walker {

	private static final HashMap<Object, Walker> map = new HashMap<Object, Walker>();
	
	public static Walker get(Object o){
		return map.get(o);
	}
	
	private RSTile next;
	private final Script script;
	private RSLocalPath path;
	private long timerLength = 15000;
	private Timer timer = new Timer(timerLength);
	private int accuracyX = 3;
	private int accuracyY = 3;

	public DefaultWalker(Script script){
		this.script = script;
		map.put(script, this);
	}
	
	@Override
	public RSTile getNext(){
		return next;
	}

	@Override
	public boolean walkTo(RSTile tile) {
		return walkTo(tile, new Checker(){
			public boolean conditionMet() {
				return false;
			}
		});
	}

	@Override
	public boolean walkTo(RSTile tile, Checker conditionMet) {
		return walkPath((RSLocalPath)script.ctx.walking.getPath(tile), conditionMet);
	}

	@Override
	public boolean walkTo(RSCharacter character) {
		return walkTo(character.getLocation());
	}

	@Override
	public boolean walkTo(RSCharacter character, Checker conditionMet) {
		return walkTo(character.getLocation(), conditionMet);
	}

	@Override
	public boolean walkTo(RSArea area) {
		return walkTo(area.getCentralTile());
	}

	@Override
	public boolean walkTo(RSArea area, Checker conditionMet) {
		return walkTo(area.getCentralTile(), conditionMet);
	}

	@Override
	public boolean walkPath(RSPath path){
		return walkPath((RSLocalPath)path);
	}

	@Override
	public boolean walkPath(RSPath path, Checker conditionMet){
		return walkPath((RSLocalPath)path, conditionMet);
	}

	private boolean walkPath(RSLocalPath localPath, Checker conditionMet){
		path = localPath;
		if(!script.ctx.calc.canReach(path.getEnd(), false)){
			script.log(Color.red, "Destination " + path.getEnd() + " is not reachable.");
			return false;
		}
		if(path == null)
			return false;
		timer.reset();
		while(timer.isRunning() && 
				script.isActive() && 
				script.ctx.calc.distanceTo(path.getEnd()) > 4 &&
				!conditionMet.conditionMet()){
			boolean skipChecks;
			if(skipChecks = path != localPath){
				script.log(Color.red, "Path changed");
				localPath = path;
			}
			final RSTile dest = script.ctx.walking.getDestination();
			if(skipChecks || dest == null || script.ctx.calc.distanceTo(dest) <= Methods.random(6, 8)){
				next = path.getNext();
				if(skipChecks || (dest == null || script.ctx.calc.distanceBetween(dest, path.getEnd()) <= 2) 
						&& path.traverse()){
					timer.reset();
				}
			}
		}
		if(!script.isActive())
			return true; //Can't necessarily say we failed if the script just paused...
		if(!timer.isRunning()){
			script.log(Color.red, "Problem getting to destination " + path.getEnd());
			return false;
		}
		return script.ctx.calc.distanceTo(path.getEnd()) <= 4 || conditionMet.conditionMet();
	}

	@Override
	public RSPath getPath() {
		return path;
	}

	@Override
	public void setPath(RSPath path) {
		this.path = (RSLocalPath) path;
	}

	@Override
	public long getTimerLength() {
		return timerLength;
	}

	@Override
	public void setTimerLength(long timerLength) {
		this.timerLength = timerLength;
		timer = new Timer(timerLength);
	}

	@Override
	public int getXAccuracy() {
		return accuracyX;
	}

	@Override
	public int getYAccuracy() {
		return accuracyY;
	}

	@Override
	public void setAccuracy(int x, int y) {
		accuracyX = x;
		accuracyY = y;
	}

}
