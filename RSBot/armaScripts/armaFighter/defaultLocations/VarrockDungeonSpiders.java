package armaScripts.armaFighter.defaultLocations;

import java.awt.Color;

import org.rsbot.script.methods.Methods;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import armaScripts.armaFighter.ArmaFighter;
import armaScripts.interfaces.Checker;

public class VarrockDungeonSpiders extends armaScripts.armaFighter.actionSets.DefaultActionSet {

	
	private static final RSTile dungeonEntrance = new RSTile(3237, 3458);
	private static final int manholeClosedId = 881;
	private static final int manholeId = 882;
	
	private boolean waitFor(Checker checker, long timeout){
		final Timer timer = new Timer(timeout);
		while(timer.isRunning() && !checker.conditionMet())
			Methods.sleep(200, 1000);
		return checker.conditionMet();
	}
	
	public VarrockDungeonSpiders(final ArmaFighter armaFighter) {
		
		
		
		/*1 - Override targget ids - DONE
		 * 2 - Override target area swX, swY, neX, neY - DONE
		 * 3 - Override walkingToTargets - DONE
		 * 4 - Override walkingToBank
		 * 5 - Override bankArea
		 * 6 - Override lootIds - DONE
		 * 7 - Tell me :D
		*/
		super(armaFighter);
		armaFighter.lootList = new int[]{223};
		armaFighter.targetArea = new RSArea(3176, 9876, 3189, 9898);
		armaFighter.targetIds = new int[]{63};
		waitFor(new Checker(){
			public boolean conditionMet(){
				return armaFighter.ctx.game.isLoggedIn();
			}
		}, 30000);
		armaFighter.bankArea = new RSArea(1, 1, 10 , 123);
	}
	
	public boolean walkToTargets(){
		if(!walker.walkTo(dungeonEntrance))
			return false;
		RSObject manhole = armaFighter.ctx.objects.getNearest(manholeClosedId);
		if(manhole != null){
			if(!manhole.doClick(true))
				return false;
		}
		if(!waitFor(new Checker(){
			public boolean conditionMet(){
				return armaFighter.ctx.objects.getNearest(manholeClosedId) == null;
				//closed manhole is no more 
			}
		}, 2000))
			return false;//you have to account for that
		manhole = armaFighter.ctx.objects.getNearest(manholeId);
		if(manhole == null)
			return false;
		if(!manhole.interact("Climb-down"))
			return false;
		if(!waitFor(new Checker(){
			public boolean conditionMet(){
				return armaFighter.ctx.interfaces.canContinue();
			}
		}, 5000))
			return false;
		armaFighter.ctx.interfaces.clickContinue();
		Methods.sleep(1000, 2500);
		armaFighter.ctx.interfaces.getComponent(1, 1).doClick(); //FIXME Interface IDs and
		Methods.sleep(500, 1500);
		return super.walkToTargets();
	}
	
	public boolean healthLow(){
		armaFighter.log(Color.red, "Health should not be low...");
		armaFighter.stopScript();
		return false;
	}

}
