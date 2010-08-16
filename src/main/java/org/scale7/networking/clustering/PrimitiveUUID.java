package org.scale7.networking.clustering;

import java.security.SecureRandom;

public class PrimitiveUUID {

	private static volatile SecureRandom numberGenerator = null;
	
	public static int getUuid32() {
        SecureRandom ng = numberGenerator;
        if (ng == null) {
            numberGenerator = ng = new SecureRandom();
        }
        
        byte[] randomBytes = new byte[4];
        ng.nextBytes(randomBytes);   
        
        int uuid = 0;
        
        for (int i=0; i<4; i++)
            uuid = (uuid << 8) | (randomBytes[i] & 0xff);           
        
        return uuid;
	}
	
	public static long getUuid64() {
        SecureRandom ng = numberGenerator;
        if (ng == null) {
            numberGenerator = ng = new SecureRandom();
        }
        
        byte[] randomBytes = new byte[8];
        ng.nextBytes(randomBytes);     
        
        long uuid = 0;
        
        for (int i=0; i<8; i++)
            uuid = (uuid << 8) | (randomBytes[i] & 0xff);   
        
        return uuid;
	}
	
}
