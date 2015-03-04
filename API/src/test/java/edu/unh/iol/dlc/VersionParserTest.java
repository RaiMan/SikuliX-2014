package edu.unh.iol.dlc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionParserTest {

   private static final String RFB_004_001 = "RFB 004.001";
   private static final String RFB_003_008 = "RFB 003.008";
   private static final String RFB_003_007 = "RFB 003.007";
   private static final String RFB_003_005 = "RFB 003.005";
   private static final String RFB_003_003 = "RFB 003.003";

   @Test(expected = IllegalArgumentException.class)
   public void nullThrowsException() {
      VersionParser parser = new VersionParser(null);
      parser.parse();
   }

   @Test(expected = IllegalArgumentException.class)
   public void shortThrowsException() {
      VersionParser parser = new VersionParser("00000000");
      parser.parse();
   }

   @Test(expected = IllegalArgumentException.class)
   public void longThrowsException() {
      VersionParser parser = new VersionParser("1234567890123");
      parser.parse();
   }

   @Test(expected = UnrecognizedVersionException.class)
   public void unrecognizedDigitVersionThrowsException() {
      VersionParser parser = new VersionParser("12345678901");
      parser.parse();
   }

   @Test(expected = IllegalArgumentException.class)
   public void unrecognizedCharacterVersionThrowsException() {
      VersionParser parser = new VersionParser("aaaaaaaaaaa");
      parser.parse();
   }

   @Test(expected = UnrecognizedVersionException.class)
   public void unsupportedVersionThrowsException() {
      VersionParser parser = new VersionParser("RFB 005.001");
      parser.parse();
   }

   @Test
   public void returnThreeThree() {
      VersionParser parser = new VersionParser(RFB_003_003);
      final ProtocolVersion parsedVersion = parser.parse();
      assertEquals(ProtocolVersion.THREE_THREE, parsedVersion);
   }

   @Test
   public void returnThreeFive() {
      VersionParser parser = new VersionParser(RFB_003_005);
      final ProtocolVersion parsedVersion = parser.parse();
      assertEquals(ProtocolVersion.THREE_FIVE, parsedVersion);
   }

   @Test
   public void returnThreeSeven() {
      VersionParser parser = new VersionParser(RFB_003_007);
      final ProtocolVersion parsedVersion = parser.parse();
      assertEquals(ProtocolVersion.THREE_SEVEN, parsedVersion);
   }

   @Test
   public void returnThreeEight() {
      VersionParser parser = new VersionParser(RFB_003_008);
      final ProtocolVersion parsedVersion = parser.parse();
      assertEquals(ProtocolVersion.THREE_EIGHT, parsedVersion);
   }

   @Test
   public void returnFourOne() {
      VersionParser parser = new VersionParser(RFB_004_001);
      final ProtocolVersion parsedVersion = parser.parse();
      assertEquals(ProtocolVersion.FOUR_ONE, parsedVersion);
   }
}
