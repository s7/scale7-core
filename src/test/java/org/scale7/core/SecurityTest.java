package org.scale7.core;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scale7.networking.security.WSSEUsernameToken;
import org.scale7.portability.SystemProxy;
import org.slf4j.Logger;

public class SecurityTest {
	private static final Logger logger = SystemProxy.getLoggerFromFactory(SecurityTest.class);
    @Test
    public void testWSSEUsernameToken() throws Exception {
    	logger.info("Testing WSSEUsernameToken...");

    	String tokenString = WSSEUsernameToken.generateUsernameToken("DomW", "nonce", new Date(0), "password");
    	logger.info("Generated token: {}", tokenString);

    	WSSEUsernameToken tokenObj = new WSSEUsernameToken();
    	tokenObj.parseFrom(tokenString);
    	logger.info("Parsed generated token");

    	assertTrue("Token does not validate using valid password", tokenObj.isSignatureValid("password"));
    }
}
