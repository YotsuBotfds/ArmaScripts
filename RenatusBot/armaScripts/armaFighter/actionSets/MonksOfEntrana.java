package armaScripts.armaFighter.actionSets;
import org.rsbot.script.concurrent.LoopTask;
import org.rsbot.script.methods.Calculations;
import org.rsbot.script.methods.NPCs;
import org.rsbot.script.methods.Players;
import org.rsbot.script.methods.Walking;
import org.rsbot.script.methods.ui.Interfaces;
import org.rsbot.script.wrappers.Area;
import org.rsbot.script.wrappers.InterfaceComponent;
import org.rsbot.script.wrappers.NPC;
import org.rsbot.script.wrappers.Path;
import org.rsbot.script.wrappers.Tile;

import armaScripts.armaFighter.ArmaFighter;


public class MonksOfEntrana extends DefaultActionSet {
	
	public MonksOfEntrana(ArmaFighter armaFighter) {
		super(armaFighter);
		armaFighter.targetArea = new Area(3043, 3480, 3061, 3499);
		armaFighter.targetIds = new int[]{7727};
		walker.setAccuracy(1, 1);
	}
	
	@Override
	public boolean healthLow(){
		while(Players.getLocal().getInteracting() != null)
			LoopTask.sleep(LoopTask.random(500, 2000));
		NPC npc = NPCs.getNearest(801);
		final Path path = Walking.getPath(npc.getLocation());
		while(!npc.isOnScreen() /*&& armaFighter.isActive()TODO*/){
			final Tile dest = Walking.getDestination();
			if(dest == null || Calculations.distanceTo(dest) <= 6)
				Walking.walkTileMM(path.getNext(), 2, 2);
		}
		LoopTask.sleep(LoopTask.random(500, 2000));
		while(armaFighter.getHPPercent() < 95){
			if(!npc.interact("Talk-to"))
				continue;
			while(Players.getLocal().isMoving())
				LoopTask.sleep(500);
			LoopTask.sleep(LoopTask.random(500, 1000));
			while(Interfaces.canContinue()){
				Interfaces.clickContinue();
				LoopTask.sleep(LoopTask.random(500, 1000));
			}
			InterfaceComponent heal = Interfaces.getComponent(230, 2);
			if(!heal.isValid() || !heal.click(true))
				return false;
			LoopTask.sleep(LoopTask.random(750, 1500));
			while(Interfaces.canContinue()){
				Interfaces.clickContinue();
				LoopTask.sleep(LoopTask.random(500, 1000));
			}
		}
		return true;
	}
}
