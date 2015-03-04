package edu.unh.iol.dlc;

public enum ProtocolVersion {

   THREE_THREE(3, 3, "RFB 003.003"),
   THREE_FIVE(3, 5, "RFB 003.007"),
   THREE_SEVEN(3, 7, "RFB 003.007"),
   THREE_EIGHT(3, 8, "RFB 003.008"),
   FOUR_ONE(4, 1, "RFB 003.008");

   final private int majorVersion;
   final private int minorVersion;
   final private String replyCode;

   ProtocolVersion(int majorVersion, int minorVersion, String replyCode) {
      this.majorVersion = majorVersion;
      this.minorVersion = minorVersion;
      this.replyCode = replyCode;
   }

   public static ProtocolVersion fromString(String protocolString) {
      VersionParser versionParser = new VersionParser(protocolString);
      ProtocolVersion parsedVersion;
      try {
         parsedVersion = versionParser.parse();
      } catch (Exception e) {
         final String error = "Error parsing protocol version: '%s'";
         final String formattedError = String.format(error, protocolString);
         throw new RuntimeException(formattedError, e);
      }

      return parsedVersion;
   }

   public int getMajorVersion() {
      return majorVersion;
   }

   public int getMinorVersion() {
      return minorVersion;
   }

   public String getReplyCode() {
      return replyCode;
   }

   @Override
   public String toString() {
      return "ProtocolVersion{" +
             "majorVersion=" + majorVersion +
             ", minorVersion=" + minorVersion +
             ", replyCode='" + replyCode + "}";
   }
}
