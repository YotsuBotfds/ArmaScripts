package armaScripts.armaFighter;

import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSNPC;

import armaScripts.interfaces.Checker;

public class ArmaFighterActionSet implements Checker {

	private RSNPC target;

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
	public ArmaFighterActionSet(ArmaFighter armaFighter){
		this.armaFighter = armaFighter;
	}

	public Filter<RSNPC> getRSNPCFilter(){
		return filter;
	}

	/**
	 * Called when the current player must walk to the npcs.
	 * Useful for when you must go up and down stairs/ladders/dungeons
	 * @return If the necessary steps were taken to walk to target npcs (or at least part of the path)
	 */
	public boolean walkToNpcAreaAction(){
		return armaFighter.walker.walkToArea(armaFighter.targetArea);
	}

	/**
	 * Called whenever the current player's inventory is full (excluding if drawing food from the bank)
	 * @return 1 if the necessary steps were taken to empty the inventory, 0 if the action was failed,
	 * or -1 to deposit the inventory
	 */
	public int inventoryFullAction(){
		return -1;
	}

	/**
	 * Called whenever the player needs to use the bank
	 * @return If the necessary steps were taken to walk to target bank (or at least part of the path)
	 */
	public boolean walkToBankAction(){
		return armaFighter.walker.walkToArea(armaFighter.bankArea);
	}

	/**
	 * @return Either <code>CUSTOM_ACTION</code>, <code>EAT</code>, <code>TELEPORT</code>, or <code>SHUTDOWN</code> of <code>ArmaFighter.HealthLowActions</code>.
	 */
	public ArmaFighter.HealthLowActions getHealthLowAction(){
		return ArmaFighter.HealthLowActions.EAT;
	}

	/**
	 * Called when you hp percent is less than the specified amount to replenish health if and ONLY if <code>getHealthLowAction()</code> returns <code>ArmaFighter.HealthLowActions.CUSTOM</code>
	 * <br></br>
	 * <b>Note: If you override this, make SURE you override <code>getHealthLowAction()</code>, otherwise the script
	 * will resort to the default action, which is eating the specified food.</b>
	 * @return If the necessary steps were taken to replenish the current player's health
	 */
	public boolean healthLowAction(){
		return false;
	}

	public boolean walkToTargetNpc(RSNPC target){
		this.target = target;
		return armaFighter.walker.walkToCharacter(target, this);
	}

	public boolean goodToContinue() {
		if(target != null && (target.getInteracting() != null || target.getHPPercent() == 0)){
			target = armaFighter.ctx.npcs.getNearest(getRSNPCFilter());
			armaFighter.target = target;
			if(target != null)
				armaFighter.path = armaFighter.ctx.walking.getPath(target.getLocation());
			else
				armaFighter.path = null;
		}
		return target != null && !target.isOnScreen();
	}

}