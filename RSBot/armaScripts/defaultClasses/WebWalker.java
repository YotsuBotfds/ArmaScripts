package armaScripts.defaultClasses;

import org.rsbot.script.Script;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSWeb;

public class WebWalker {
	
	private final Script script;
	private RSWeb web;
	private RSTile last = new RSTile(0, 0);
	
	public WebWalker(Script script){
		this.script = script;
	}
	
	public void walkTo(RSTile test){
		if(!test.equals(last))
			web = script.ctx.web.getWeb(test);
		web.step();
	}

}
