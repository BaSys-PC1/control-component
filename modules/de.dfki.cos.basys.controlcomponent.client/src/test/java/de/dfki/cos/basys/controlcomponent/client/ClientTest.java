package de.dfki.cos.basys.controlcomponent.client;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.l;
import static org.eclipse.milo.opcua.stack.core.util.FutureUtils.failedFuture;
import static org.junit.Assert.*;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.methods.UaMethod;
import org.eclipse.milo.opcua.sdk.client.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaObjectNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.dfki.cos.basys.common.component.Component;
import de.dfki.cos.basys.common.component.ComponentContext;
import de.dfki.cos.basys.common.component.ComponentException;
import de.dfki.cos.basys.common.component.StringConstants;
import de.dfki.cos.basys.controlcomponent.ComponentOrderStatus;
import de.dfki.cos.basys.controlcomponent.ExecutionMode;
import de.dfki.cos.basys.controlcomponent.ExecutionState;
import de.dfki.cos.basys.controlcomponent.OccupationState;
import de.dfki.cos.basys.controlcomponent.OccupationStatus;
import de.dfki.cos.basys.controlcomponent.OrderStatus;
import de.dfki.cos.basys.controlcomponent.ParameterInfo;
import de.dfki.cos.basys.controlcomponent.client.opcua.nodes.ControlComponentNode;
import de.dfki.cos.basys.controlcomponent.client.opcua.nodes.ControlComponentOperationsNode;
import de.dfki.cos.basys.controlcomponent.client.opcua.nodes.ControlComponentStatusNode;
import de.dfki.cos.basys.controlcomponent.server.opcua.ControlComponentServer;
import de.dfki.cos.basys.controlcomponent.client.opcua.types.ControlComponentStatusDataType;

public class ClientTest extends BaseTest {

	private boolean serverRequired = false;
	private ControlComponentServer server;
	private ControlComponentClientImpl client;
	private OpcUaClient opcuaClient;

	String occupier = "occupier";
	String opmode = "REMOVE";

	@Before
	public void setUp() throws Exception {
		if (serverRequired) {
			server = new ControlComponentServer();
			server.startup().get();
		}

		Properties config = new Properties();
		config.put("nodeId", "baxter-1/ControlComponent");
		config.put(StringConstants.serviceConnectionString, "opc.tcp://127.0.0.1:12685/basys");
		client = new ControlComponentClientImpl(config, null);
		client.connect(ComponentContext.getStaticContext(),
				config.getProperty(StringConstants.serviceConnectionString));
		opcuaClient = client.getChannel().getClient();
	}

	@After
	public void tearDown() throws Exception {
		client.disconnect();
		if (serverRequired && server != null) {
			server.shutdown().get();
			server = null;
		}
		Stack.releaseSharedResources();
	}

	@Test
	@Ignore
	public void getControlComponentNode() throws InterruptedException, ExecutionException {
		ControlComponentNode node = client.getControlComponentNode();
		assertEquals("ControlComponent", node.getBrowseName().getName());
		ControlComponentStatusNode statusNode = node.getControlComponentStatusNode();
		ControlComponentStatusDataType statusValue = node.getControlComponentStatus();
//		System.out.println("----------");
//		System.out.println(statusValue);
//		System.out.println("----------");
		assertEquals(
				"ControlComponentStatusDataType{ERRCODE=0, ERRMSG=OK, EXMODE=SIMULATE, EXSTATE=STOPPED, OCCST=FREE, OCCUPIER=INIT, OPMODE=default, WORKST=}",
				statusValue.toString());
		assertEquals("FREE", statusNode.getOccupationState());
		assertEquals("INIT", statusNode.getOccupierId());
	}

	@Test
	@Ignore
	public void testReadStatus() {
		int errorCode = client.getErrorCode();
		assertEquals(0, errorCode);
		String errorMessage = client.getErrorMessage();
		assertEquals("OK", errorMessage);

		OccupationState level = client.getOccupationState();
		assertEquals(OccupationState.FREE, level);
		String occupierId = client.getOccupierId();
		assertEquals("INIT", occupierId);

		ExecutionMode exmode = client.getExecutionMode();
		assertEquals(ExecutionMode.SIMULATE, exmode);
		ExecutionState state = client.getExecutionState();
		assertEquals(ExecutionState.STOPPED, state);

		String opmode = client.getOperationMode().getName();
		assertEquals("BSTATE", opmode);

		String workState = client.getWorkState();
		assertEquals("", workState);
	}

	@Test
	@Ignore
	public void testSetOperationMode() {
		ComponentOrderStatus status = null;

		try {			
			status = client.occupy();
			assertEquals(status.getMessage(), OrderStatus.DONE, status.getStatus());
			Thread.sleep(1000);

			OccupationState level = client.getOccupationState();
			assertEquals(OccupationState.OCCUPIED, level);
			//String occupierId = client.getOccupierId();
			//assertEquals(occupier, occupierId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			status = client.reset();
			assertEquals(status.getMessage(), OrderStatus.ACCEPTED, status.getStatus());
			Thread.sleep(2000);

			ExecutionMode exmode = client.getExecutionMode();
			assertEquals(ExecutionMode.SIMULATE, exmode);
			ExecutionState state = client.getExecutionState();
			assertEquals(ExecutionState.IDLE, state);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			status = client.setOperationMode(opmode);
			assertEquals(status.getMessage(), OrderStatus.DONE, status.getStatus());
			Thread.sleep(1000);

			String mode = client.getOperationMode().getName();
			assertEquals(opmode, mode);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int errorCode = client.getErrorCode();
		assertEquals(0, errorCode);
		String errorMessage = client.getErrorMessage();
		assertEquals("OK", errorMessage);
		String workState = client.getWorkState();
		assertEquals("", workState);
		
		try {
			status = client.stop();
			assertEquals(status.getMessage(), OrderStatus.ACCEPTED, status.getStatus());
			Thread.sleep(1000);

			ExecutionMode exmode = client.getExecutionMode();
			assertEquals(ExecutionMode.SIMULATE, exmode);
			ExecutionState state = client.getExecutionState();
			assertEquals(ExecutionState.STOPPED, state);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {			
			status = client.free();
			assertEquals(status.getMessage(), OrderStatus.DONE, status.getStatus());
			Thread.sleep(1000);

			OccupationState level = client.getOccupationState();
			assertEquals(OccupationState.FREE, level);
			//String occupierId = client.getOccupierId();
			//assertEquals(occupier, occupierId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	@Test
	@Ignore
	public void testReadProperty() {
		ParameterInfo info = null;
		try {
			info = client.getParameter("duration");
			System.out.println(info);
		} catch (ComponentException e) {
			e.printStackTrace();
		}
		System.out.println("--------");
	}
}
