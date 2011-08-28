package armaScripts.armaFighter.actionSets;
import org.rsbot.script.methods.Methods;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSTile;

import armaScripts.armaFighter.ArmaFighter;


public class MonksOfEntrana extends DefaultActionSet {
	
	public MonksOfEntrana(ArmaFighter armaFighter) {
		super(armaFighter);
		armaFighter.targetArea = new RSArea(3043, 3480, 3061, 3499);
		armaFighter.targetIds = new int[]{7727};
		walker.setAccuracy(1, 1);
	}
	
	@Override
	public boolean healthLow(){
		while(armaFighter.getMyPlayer().getInteracting() != null)
			Methods.sleep(500, 2000);
		RSNPC npc = armaFighter.ctx.npcs.getNearest(801);
		final RSPath path = armaFighter.ctx.walking.getPath(npc.getLocation());
		while(!npc.isOnScreen() && armaFighter.isActive()){
			final RSTile dest = armaFighter.ctx.walking.getDestination();
			if(dest == null || armaFighter.ctx.calc.distanceTo(dest) <= 6)
				armaFighter.ctx.walking.walkTileMM(path.getNext(), 2, 2);
		}
		Methods.sleep(500, 2000);
		while(armaFighter.getHPPercent() < 95){
			if(!npc.interact("Talk-to"))
				continue;
			while(armaFighter.getMyPlayer().isMoving())
				Methods.sleep(500);
			Methods.sleep(500, 1000);
			while(armaFighter.ctx.interfaces.canContinue()){
				armaFighter.ctx.interfaces.clickContinue();
				Methods.sleep(500, 1000);
			}
			RSComponent heal = armaFighter.ctx.interfaces.getComponent(230, 2);
			if(!heal.isValid() || !heal.doClick(true))
				return false;
			Methods.sleep(750, 1500);
			while(armaFighter.ctx.interfaces.canContinue()){
				armaFighter.ctx.interfaces.clickContinue();
				Methods.sleep(500, 1000);
			}
		}
		return true;
	}
}
