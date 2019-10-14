package de.dfki.cos.basys.controlcomponent.test;

import de.dfki.cos.basys.controlcomponent.annotation.Parameter;
import de.dfki.cos.basys.controlcomponent.impl.BaseControlComponent;
import de.dfki.cos.basys.controlcomponent.impl.BaseOperationMode;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import de.dfki.cos.basys.controlcomponent.ControlComponent;
import de.dfki.cos.basys.controlcomponent.ExecutionCommand;
import de.dfki.cos.basys.controlcomponent.VariableAccess;
import de.dfki.cos.basys.controlcomponent.annotation.OperationMode;

@OperationMode(description = "move to a location", name = "movetolocation", shortName = "MOVE", allowedCommands = {
		ExecutionCommand.ABORT, ExecutionCommand.CLEAR, ExecutionCommand.HOLD, ExecutionCommand.RESET,
		ExecutionCommand.START, ExecutionCommand.STOP, ExecutionCommand.SUSPEND, ExecutionCommand.UNHOLD,
		ExecutionCommand.UNSUSPEND })
public class MoveOperationMode extends BaseOperationMode {

	@Parameter(name = "location", access = VariableAccess.READ_WRITE)
	private String location = "";

	@Parameter(name = "duration", access = VariableAccess.READ_ONLY)
	private int duration = 0;
	
	public MoveOperationMode(BaseControlComponent component) {
		super(component);
	}


	@Override
	public void onResetting() {
		sleep(1000);
	}

	@Override
	public void onStarting() {
		LOGGER.info("move to " + location);
		sleep(1000);
	}

	@Override
	public void onExecute() {
		sleep(1000);
	}

	@Override
	public void onCompleting() {
		duration = 5000;
		sleep(1000);
	}

	@Override
	public void onHolding() {
		sleep(1000);
	}

	@Override
	public void onUnholding() {
		sleep(1000);
	}

	@Override
	public void onSuspending() {
		sleep(1000);
	}

	@Override
	public void onUnsuspending() {
		sleep(1000);
	}

	@Override
	public void onAborting() {
		sleep(1000);
	}

	@Override
	public void onClearing() {
		sleep(1000);
	}

	@Override
	public void onStopping() {
		sleep(1000);
	}

	public void sleep(long millis) {
		try {
			TimeUnit.MILLISECONDS.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
