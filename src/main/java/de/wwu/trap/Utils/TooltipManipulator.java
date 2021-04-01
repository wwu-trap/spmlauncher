package de.wwu.trap.Utils;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class TooltipManipulator {

	public static void makeTooltipInstant(Tooltip tt){
		tt.setShowDelay(Duration.seconds(0.05));
	}
	
}
