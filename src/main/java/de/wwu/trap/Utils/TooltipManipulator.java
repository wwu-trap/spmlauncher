package de.wwu.trap.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class TooltipManipulator {

	public static void makeTooltipInstant(Tooltip tt){
		try {
	        Class<?> clazz = tt.getClass().getDeclaredClasses()[0];
	        Constructor<?> constructor = clazz.getDeclaredConstructor(
	                Duration.class,
	                Duration.class,
	                Duration.class,
	                boolean.class);
	        constructor.setAccessible(true);
	        Object tooltipBehavior = constructor.newInstance(
	                new Duration(50),  //open
	                new Duration(10000), //visible
	                new Duration(20),  //close
	                false);
	        Field fieldBehavior = tt.getClass().getDeclaredField("BEHAVIOR");
	        fieldBehavior.setAccessible(true);
	        fieldBehavior.set(tt, tooltipBehavior);
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
}
