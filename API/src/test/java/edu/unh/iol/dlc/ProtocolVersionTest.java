package edu.unh.iol.dlc;

import org.junit.Assert;
import org.junit.Test;

public class ProtocolVersionTest {

   private static final String RFB_003_003 = "RFB 003.003";
   private static final String INVALID_PROTOCOL = "123456789012";

   @Test(expected = RuntimeException.class)
   public void throwErrorWhenNullIsPassed() {
      ProtocolVersion.fromString(null);
   }

   @Test(expected = RuntimeException.class)
   public void throwErrorWhenInvalidCodeIsPassed() {
      ProtocolVersion.fromString(INVALID_PROTOCOL);
   }

   @Test
   public void getCorrectCode() {
      final ProtocolVersion protocolVersion = ProtocolVersion.fromString(
            RFB_003_003);
      Assert.assertEquals(ProtocolVersion.THREE_THREE, protocolVersion);
   }
}
