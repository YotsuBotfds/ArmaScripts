package armaScripts.wrappers;

import org.rsbot.script.wrappers.RSNPC;

public class NPCWrapper implements Comparable<NPCWrapper> {

	public final RSNPC npc;
	public final String name;
	public final int level;
	public final int id;

	public NPCWrapper(RSNPC npc){
		this.npc = npc;
		name = npc.getName();
		level = npc.getLevel();
		id = npc.getID();
	}

	public String toString(){
		return name + ": Level " + level;
	}

	public int compareTo(NPCWrapper npc) {
		if(npc == null)
			return -1;
		return name.compareTo(npc.name);
	}

	public boolean equals(Object o){
		if(!(o instanceof NPCWrapper))
			return false;
		return ((NPCWrapper)o).hashCode() == hashCode();
	}

	public int hashCode(){
		return level * 31 + name.hashCode();
	}

}