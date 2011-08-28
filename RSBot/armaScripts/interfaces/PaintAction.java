package armaScripts.interfaces;

import java.awt.Graphics;

public abstract class PaintAction {
	
	private Checker paintWhile;
	private long startTime;
	
	public PaintAction(Checker paintWhile){
		this.paintWhile = paintWhile;
		startTime = System.currentTimeMillis();
	}
	
	public final boolean shouldRemove(){
		return true;
	}
	
	public abstract void paint(Graphics g);

}
