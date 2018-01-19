package cn.xian.app;

import top.onceio.annotation.BeansIn;
import top.onceio.mvc.Webapp;


@BeansIn("cn.xian.app")
public class Launcher {
	
	public static void main(String[] args) {
		Webapp.run(Launcher.class, args);
	}
}
