package de.dfki.cos.basys.controlcomponent;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.cos.basys.common.component.Component;
import de.dfki.cos.basys.common.component.ComponentContext;
import de.dfki.cos.basys.common.component.ComponentOrderStatus;
import de.dfki.cos.basys.controlcomponent.impl.BaseControlComponent;
import de.dfki.cos.basys.controlcomponent.util.ControlComponentInfoRecorder;
import de.dfki.cos.basys.controlcomponent.util.TestControlComponent;

public class BaseTest {

	protected final Logger LOGGER = LoggerFactory.getLogger("ControlComponentTests");
	
	protected Properties config;
	protected BaseControlComponent component;
	protected ControlComponentInfoRecorder recorder;
	protected ControlComponentInfo info = null;
	protected ComponentOrderStatus status = null;
	
	protected String user_a = "user_a";
	protected String user_b = "user_b";
	protected String occupier = "occupier";
	
	@Before
	public void setUp() throws Exception {

		config = new Properties();
		config.put(Component.id, "id");
		config.put(Component.name, "name");
		config.put(Component.connectionString, "");

		component = new TestControlComponent(config);
		component.activate(ComponentContext.getStaticContext());

		recorder = new ControlComponentInfoRecorder();
	}

	@After
	public void tearDown() throws Exception {
		component.deactivate();
	}
}
