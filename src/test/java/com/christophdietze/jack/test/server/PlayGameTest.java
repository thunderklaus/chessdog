package com.christophdietze.jack.test.server;

import junit.framework.TestCase;

import org.junit.Test;

import com.christophdietze.jack.server.ChessServiceImpl;
import com.christophdietze.jack.shared.LoginResponse.LoginSuccessfulResponse;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class PlayGameTest extends TestCase {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Override
	protected void setUp() throws Exception {
		helper.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void test1() {
		Injector injector = Guice.createInjector();
		ChessServiceImpl chessService = injector.getInstance(ChessServiceImpl.class);

		long locationId1 = ((LoginSuccessfulResponse) chessService.login("Alice")).getLocationId();
		chessService.loginComplete(locationId1);
		long locationId2 = ((LoginSuccessfulResponse) chessService.login("Bob")).getLocationId();
		chessService.loginComplete(locationId2);

		chessService.postChallenge(locationId1, "Bob");

		// TODO i need to receive the comet message here to know the challenge id
		// chessService.makeMove(locationId2, "e2e4");
		// chessService.makeMove(locationId1, "e7e5");
	}

}
