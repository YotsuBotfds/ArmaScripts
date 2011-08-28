package armaScripts.interfaces;

import java.awt.Graphics;

import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.NPC;

public interface ArmaFighterActionSet {
	
	public Filter<NPC> getNPCFilter();
	
	public boolean walkToTargets();
	
	public boolean inventoryFull();
	
	public boolean walkToBank();
	
	public boolean healthLow();
	
	public boolean walkToTarget();
	
	public void paint(Graphics g);
	
	public Walker getWalker();

}
