package armaScripts.defaultClasses;

import java.awt.Color;
import java.util.HashMap;

import org.rsbot.script.Script;
import org.rsbot.script.concurrent.LoopTask;
import org.rsbot.script.methods.Calculations;
import org.rsbot.script.methods.Walking;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.Area;
import org.rsbot.script.wrappers.Character;
import org.rsbot.script.wrappers.NodePath;
import org.rsbot.script.wrappers.Path;
import org.rsbot.script.wrappers.Tile;

import armaScripts.interfaces.Checker;
import armaScripts.interfaces.Walker;

public class DefaultWalker implements Walker {

	private static final HashMap<Object, Walker> map = new HashMap<Object, Walker>();
	
	public static Walker get(Object o){
		return map.get(o);
	}
	
	private Tile next;
	private final Script script;
	private NodePath path;
	private long timerLength = 15000;
	private Timer timer = new Timer(timerLength);
	private int accuracyX = 3;
	private int accuracyY = 3;

	public DefaultWalker(Script script){
		this.script = script;
		map.put(script, this);
	}
	
	@Override
	public Tile getNext(){
		return next;
	}

	@Override
	public boolean walkTo(final Tile tile) {
		return walkTo(tile, new Checker(){
			public boolean conditionMet() {
				return Calculations.pointOnScreen(Calculations.worldToScreen(tile.getX(), tile.getY(), tile.getZ()));
			}
		});
	}

	@Override
	public boolean walkTo(Tile tile, Checker conditionMet) {
		return walkPath((NodePath)Walking.getPath(tile), conditionMet);
	}

	@Override
	public boolean walkTo(Character character) {
		return walkTo(character.getLocation());
	}

	@Override
	public boolean walkTo(Character character, Checker conditionMet) {
		return walkTo(character.getLocation(), conditionMet);
	}

	@Override
	public boolean walkTo(Area area) {
		return walkTo(area.getCentralTile());
	}

	@Override
	public boolean walkTo(Area area, Checker conditionMet) {
		return walkTo(area.getCentralTile(), conditionMet);
	}

	@Override
	public boolean walkPath(Path path){
		return walkPath((NodePath)path);
	}

	@Override
	public boolean walkPath(Path path, Checker conditionMet){
		return walkPath((NodePath)path, conditionMet);
	}

	private boolean walkPath(NodePath localPath, Checker conditionMet){
		path = localPath;
		if(path == null)
			return false;
		if(!Calculations.canReach(path.getEnd(), false)){
			script.log(Color.red, "Destination " + path.getEnd() + " is not reachable.");
			return false;
		}
		if(path == null)
			return false;
		timer.reset();
		while(timer.isRunning() && 
				//script.isActive() && 
				Calculations.distanceTo(path.getEnd()) > 4 &&
				!conditionMet.conditionMet()){
			boolean skipChecks;
			if(skipChecks = path != localPath){
				if(path == null)
					return true;
				script.log(Color.red, "Path changed");
				localPath = path;
			}
			final Tile dest = Walking.getDestination();
			if(skipChecks || dest == null || Calculations.distanceTo(dest) <= LoopTask.random(6, 8)){
				if(skipChecks = path != localPath){
					script.log(Color.red, "Path changed");
					localPath = path;
				}
				next = path.getNext();
				if(skipChecks || (dest == null || Calculations.distanceBetween(dest, path.getEnd()) <= 2) 
						&& path.traverse()){
					timer.reset();
				}
			}
		}
		//if(!script.isActive())
			//return true; //Can't necessarily say we failed if the script just paused...
		if(!timer.isRunning()){
			script.log(Color.red, "Problem getting to destination " + path.getEnd());
			return false;
		}
		return Calculations.distanceTo(path.getEnd()) <= 4 || conditionMet.conditionMet();
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public void setPath(Path path) {
		this.path = (NodePath) path;
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
