package armaScripts.utilities;

import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Skills;

public class Teleports {

	private static final int FIRE = 554;
	private static final int WATER = 555;
	private static final int AIR = 556;
	private static final int EARTH = 557;
	private static final int LAW = 563;
	private static final int BANANA = 1963;
	private static final int[] GAMES_NECKLACE = {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867};
	private static final int[] RING_OF_DUELING = {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566};	
	private static final int[] AMULET_OF_GLORY = {1706, 1708, 1710, 1712};
	private static final int VARROCK_TABLET = 8007;
	private static final int LUMBRIDGE_TABLET = 8008;
	private static final int FALADOR_TABLET = 8009;
	private static final int CAMELOT_TABLET = 8010;
	private static final int ARDOUGNE_TABLET = 8011;
	private static final int WATCHTOWER_TABLET = 8012;
	private static final int HOUSE_TABLET = 8013;

	public enum Type{
		LUMBRIDGE_HOME(0),
		VARROCK(25, FIRE, LAW, AIR, AIR, AIR), 
		LUMBRIDGE(31, EARTH, LAW, AIR, AIR, AIR), 
		FALADOR(37, WATER, LAW, AIR, AIR, AIR), 
		HOUSE(40, AIR, EARTH, LAW), 
		CAMELOT(45, AIR, AIR, AIR, AIR, AIR, LAW), 
		ARDOUGNE(51, LAW, LAW, WATER, WATER), 
		WATCHTOWER(58, LAW, LAW, EARTH, EARTH), 
		TROLLHEIM(61, LAW, LAW, FIRE, FIRE), 
		APE_ATOLL(64, LAW, LAW, WATER, WATER, FIRE, FIRE, BANANA), //Standard spell book
		BURTHROPE(0, false, GAMES_NECKLACE), 
		CORPORAL_BEAST(0, false, GAMES_NECKLACE), 
		GAMERS_GROTTO(0, false, GAMES_NECKLACE), 
		BARBARIAN_OUTPOST(0, false, GAMES_NECKLACE), //Games necklace
		DUEL_ARENA(0, false, RING_OF_DUELING), 
		CASTLE_WARS(0, false, RING_OF_DUELING), 
		FIST_OF_GUTHIX(0, false, RING_OF_DUELING), 
		MOBILISING_ARMIES(0, false, RING_OF_DUELING), //Ring of dueling
		EDGEVILLE(0, false, AMULET_OF_GLORY), 
		KARAMJA(0, false, AMULET_OF_GLORY), 
		DRAYNOR_VILLAGE(0, false, AMULET_OF_GLORY),
		AL_KHARID(0, false, AMULET_OF_GLORY), //Amulet of glory
		VARROCK_TABLET(0, Teleports.VARROCK_TABLET),
		LUMBRIDGE_TABLET(0, Teleports.LUMBRIDGE_TABLET), 
		FALADOR_TABLET(0, Teleports.FALADOR_TABLET), 
		CAMELOT_TABLET(0, Teleports.CAMELOT_TABLET), 
		ARDOUGNE_TABLET(0, Teleports.ARDOUGNE_TABLET),
		WATCHTOWER_TABLET(0, Teleports.WATCHTOWER_TABLET), 
		HOUSE_TABLET(0, Teleports.HOUSE_TABLET);

		public final int magicLevel;
		public final boolean needAllItems;
		public final int[] items;

		Type(int magicLevel, int...items){
			this(magicLevel, false, items);
		}

		Type(int magicLevel, boolean needAllItems, int...items){
			this.magicLevel = magicLevel;
			this.needAllItems = needAllItems;
			this.items = items;
		}
	}
	
	private static int countOfX(int[] array, int toCount){
		int count = 0;
		for(int i : array){
			if(i == toCount){
				count++;
			}
		}
		return count;
	}

	public static boolean withdrawItems(MethodContext ctx, Teleports.Type type){
		if(canTeleport(ctx, type))
			return true;
		if(!type.needAllItems){
			for(int i = type.items.length - 1; i >= 0; i--){
				if(ctx.bank.withdraw(type.items[i], 1)){
					return true;
				}
			}
			return false;
		}else{
			for(int i = 0, j = countOfX(type.items, i); i < type.items.length; i += j){
				if(!ctx.bank.withdraw(i, 1)){
					return false;
				}
			}
			return true;
		}
	}

	public static boolean canTeleport(MethodContext ctx, Teleports.Type type){
		if(ctx.skills.getCurrentLevel(Skills.MAGIC) < type.magicLevel)
			return false;
		if(!type.needAllItems)
			return ctx.inventory.containsOneOf(type.items) || ctx.equipment.containsOneOf(type.items);
		for(int i = 0, j = countOfX(type.items, i); i < type.items.length; i += j){
			if(!ctx.inventory.contains(i)){
				return false;
			}
		}
		return true;
	}
	
	public static boolean teleport(MethodContext ctx, Teleports.Type type){
		return false;
		/*if(!canTeleport(ctx, type))
			return false;
		if(type.ordinal() < 10){
			
		}else if(type.ordinal() < 14){
			
		}else if(type.ordinal() < 18){
			
		}else if(type.ordinal() < 22){
			if(ctx.inventory.containsOneOf(AMULET_OF_GLORY)){
				
			}else{
				
			}
		}else if(type.ordinal() < 29){
			return ctx.inventory.getItem(type.items).doClick(true);
		}*/
	}
}