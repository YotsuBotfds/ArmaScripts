package armaScripts.interfaces;

import java.awt.Graphics;

import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSNPC;

public interface ArmaFighterActionSet {
	
	public Filter<RSNPC> getRSNPCFilter();
	
	public boolean walkToTargets();
	
	public boolean inventoryFull();
	
	public boolean walkToBank();
	
	public boolean healthLow();
	
	public boolean walkToTarget();
	
	public void paint(Graphics g);
	
	public Walker getWalker();

}
