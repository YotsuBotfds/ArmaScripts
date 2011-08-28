package armaScripts.armaFighter.actionSets;

import java.awt.Color;
import java.awt.Graphics;

import org.rsbot.script.methods.NPCs;
import org.rsbot.script.methods.Players;
import org.rsbot.script.methods.Walking;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.NPC;

import armaScripts.armaFighter.ArmaFighter;
import armaScripts.defaultClasses.DefaultPainter;
import armaScripts.defaultClasses.DefaultWalker;
import armaScripts.interfaces.ArmaFighterActionSet;
import armaScripts.interfaces.Checker;
import armaScripts.interfaces.Painter;
import armaScripts.interfaces.Walker;

public class DefaultActionSet implements ArmaFighterActionSet, Checker {

	protected Walker walker;
	protected Painter painter;
	protected final ArmaFighter armaFighter;
	protected Filter<NPC> filter = new Filter<NPC>(){
		public boolean accept(NPC npc){
			if(npc.getInteracting() != null){
				if(npc.getInteracting().equals(Players.getLocal().getLocation()))
					return true;
				return false;
			}
			if(npc.getHPPercent() == 0)
				return false;
			final int npcId = npc.getID();
			for(int id : armaFighter.targetIds){
				if(npcId == id){
					return true;
				}
			}
			for(String action : npc.getActions())
				if(action != null && action.equals("Attack"))
					return armaFighter.targetArea.contains(npc.getLocation());
			return false;
		}
	};

	/**
	 * Make sure when overriding this class that you initialize the superclass with super(ArmaFighter),
	 * otherwise default actions will cause an exception and crash the script.
	 */
	public DefaultActionSet(ArmaFighter armaFighter){
		this.armaFighter = armaFighter;
		painter = new DefaultPainter(armaFighter);
		walker = new DefaultWalker(armaFighter);
	}

	public Filter<NPC> getNPCFilter(){
		return filter;
	}

	/**
	 * Called when the current player must walk to the NPCs.
	 * Useful for when you must go up and down stairs/ladders/dungeons
	 * @return If the necessary steps were taken to walk to target NPCs (or at least part of the path)
	 */
	public boolean walkToTargets(){
		return walker.walkTo(armaFighter.targetArea);
	}

	public boolean inventoryFull(){
		return false;
	}

	/**
	 * Called whenever the player needs to use the bank
	 * @return If the necessary steps were taken to walk to target bank (or at least part of the path)
	 */
	public boolean walkToBank(){
		return walker.walkTo(armaFighter.bankArea);
	}

	/**
	 * Called when you hp percent is less than the specified amount to replenish health if and ONLY if <code>getHealthLowAction()</code> returns <code>ArmaFighter.HealthLowActions.CUSTOM</code>
	 * <br></br>
	 * <b>Note: If you override this, make SURE you override <code>getHealthLowAction()</code>, otherwise the script
	 * will resort to the default action, which is eating the specified food.</b>
	 * @return If the necessary steps were taken to replenish the current player's health
	 */
	public boolean healthLow(){
		return armaFighter.eatFood();
	}

	public boolean walkToTarget(){
		return walker.walkTo(armaFighter.target, this);
	}

	public boolean conditionMet() {
		final NPC npc = NPCs.getNearest(getNPCFilter());
		if(npc != null && !npc.getLocation().equals(armaFighter.target.getLocation())){
			armaFighter.target = npc;
			walker.setPath(Walking.getPath(armaFighter.target.getLocation()));
		}else if(armaFighter.target != null && (armaFighter.target.getInteracting() != null || armaFighter.target.getHPPercent() == 0)){
			armaFighter.target = NPCs.getNearest(getNPCFilter());
			walker.setPath(armaFighter.target == null ? null : Walking.getPath(armaFighter.target.getLocation()));
		}
		return armaFighter.target != null && armaFighter.target.isOnScreen();
	}
	
	public void paint(Graphics g) {
		painter.drawAreaOnMap(g, armaFighter.targetArea);
		if(armaFighter.target != null){
			painter.drawTileOnMap(g, armaFighter.target.getLocation(), Color.red);
			painter.drawTargetable(g, armaFighter.target);
		}
		painter.paint(g);
	}
	
	public Walker getWalker() {
		return walker;
	}

}