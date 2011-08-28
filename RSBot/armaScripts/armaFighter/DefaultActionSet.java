package armaScripts.armaFighter;

import java.awt.Graphics;

import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSNPC;

import armaScripts.interfaces.ArmaFighterActionSet;
import armaScripts.interfaces.Checker;
import armaScripts.interfaces.Painter;
import armaScripts.interfaces.Walker;

public class DefaultActionSet implements ArmaFighterActionSet, Checker {

	protected Walker walker;
	protected Painter painter;
	protected final ArmaFighter armaFighter;
	protected Filter<RSNPC> filter = new Filter<RSNPC>(){
		public boolean accept(RSNPC npc){
			if(npc.getInteracting() != null){
				if(npc.getInteracting().equals(armaFighter.getMyPlayer().getLocation()))
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

	public Filter<RSNPC> getRSNPCFilter(){
		return filter;
	}

	/**
	 * Called when the current player must walk to the npcs.
	 * Useful for when you must go up and down stairs/ladders/dungeons
	 * @return If the necessary steps were taken to walk to target npcs (or at least part of the path)
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
		return false;
	}

	public boolean walkToTarget(){
		return walker.walkTo(armaFighter.target, this);
	}

	public boolean conditionMet() {
		final RSNPC npc = armaFighter.ctx.npcs.getNearest(getRSNPCFilter());
		if(npc != null && !npc.getLocation().equals(armaFighter.target.getLocation())){
			armaFighter.target = npc;
			walker.setPath(armaFighter.ctx.walking.getPath(armaFighter.target.getLocation()));
		}else if(armaFighter.target != null && (armaFighter.target.getInteracting() != null || armaFighter.target.getHPPercent() == 0)){
			armaFighter.target = armaFighter.ctx.npcs.getNearest(getRSNPCFilter());
			walker.setPath(armaFighter.target == null ? null : armaFighter.ctx.walking.getPath(armaFighter.target.getLocation()));
		}
		return armaFighter.target != null && armaFighter.target.isOnScreen();
	}
	
	public void paint(Graphics g) {
		painter.drawAreaOnMap(g, armaFighter.targetArea);
		if(armaFighter.target != null){
			painter.drawTileOnMap(g, armaFighter.target.getLocation());
			painter.drawCharacter(g, armaFighter.target);
		}
		painter.paint(g);
	}

}