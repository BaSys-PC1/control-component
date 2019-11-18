package de.dfki.cos.basys.controlcomponent.mir;

import de.dfki.cos.basys.controlcomponent.annotation.Parameter;
import de.dfki.cos.basys.controlcomponent.impl.BaseControlComponent;
import de.dfki.cos.basys.controlcomponent.impl.BaseOperationMode;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

import de.dfki.cos.basys.common.mirrestclient.MiRState;
import de.dfki.cos.basys.common.mirrestclient.MirService;
import de.dfki.cos.basys.common.mirrestclient.dto.MissionInstanceInfo;
import de.dfki.cos.basys.common.mirrestclient.dto.Status;
import de.dfki.cos.basys.controlcomponent.ExecutionCommand;
import de.dfki.cos.basys.controlcomponent.ExecutionMode;
import de.dfki.cos.basys.controlcomponent.VariableAccess;
import de.dfki.cos.basys.controlcomponent.annotation.OperationMode;

@OperationMode(name = "movesym", shortName = "MVSYM", description = "moves MiR to a symbolic position", 
		allowedCommands = {	ExecutionCommand.HOLD, ExecutionCommand.RESET, ExecutionCommand.START, ExecutionCommand.STOP }, 
		allowedModes = { ExecutionMode.PRODUCTION, ExecutionMode.SIMULATION })
public class MoveToSymbolicPositionOperationMode extends BaseOperationMode {

	@Parameter(name = "position", access = VariableAccess.READ_WRITE)
	private String position = null;
	
	@Parameter(name = "duration", access = VariableAccess.READ_ONLY)
	private int duration = 0;
	
	private MirService service = null;
	private MissionInstanceInfo currentMission = null;
	
	private long startTime = 0;
	private long endTime = 0;
		
	public MoveToSymbolicPositionOperationMode(BaseControlComponent component) {
		super(component);
		service = component.getServiceManager().getServiceInterface(MirService.class);
	}

	@Override
	public void onResetting() {
		duration = 0;
		startTime = 0;
		endTime = 0;
		currentMission = null;
		try {
			Status status = service.setRobotStatus(MiRState.READY);		
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getLocalizedMessage());
			component.setErrorStatus(3, e.getLocalizedMessage());
			component.stop(component.getOccupierId());
		}
	}

	@Override
	public void onStarting() {		
		startTime = System.currentTimeMillis();	
		try {
			currentMission = service.gotoSymbolicPosition(position);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getLocalizedMessage());
			component.setErrorStatus(3, e.getLocalizedMessage());
			component.stop(component.getOccupierId());
		}
	}

	@Override
	public void onExecute() {
		try {
			boolean executing = true;
			while(executing) {
				currentMission = service.getMissionInstanceInfo(currentMission.id);
				LOGGER.debug("MissionState is " + currentMission.state);
				 
				switch (currentMission.state.toLowerCase()) {
				case "pending":
					break;
				case "executing":
					break;
				case "done":
					executing=false;
					break;
				case "failed":
					executing=false;
					component.setErrorStatus(1, "failed");
					component.stop(component.getOccupierId());
					break;
				case "aborted":
					executing=false;
					component.setErrorStatus(2, "aborted");
					component.stop(component.getOccupierId());
					break;
				default:
					break;
				}
	
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getLocalizedMessage());
			component.setErrorStatus(3, e.getLocalizedMessage());
			component.stop(component.getOccupierId());
		}
	}

	@Override
	public void onCompleting() {
		endTime = System.currentTimeMillis();
		duration = (int) (endTime - startTime);
	}

	@Override
	public void onStopping() {
		endTime = System.currentTimeMillis();
		duration = (int) (endTime - startTime);
		try {
			Status status = service.setRobotStatus(MiRState.PAUSED);
			if (currentMission != null) {
				service.dequeueMissionInstance(currentMission.id);
				currentMission = null;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getLocalizedMessage());
		}	
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