package edu.unh.iol.dlc;

/**
 * Version parser utility class.  Take a VNC version in the form "RFB 00X.00Y",
 * where X is the major version and Y is the minor one.
 */
public class VersionParser {
   private static final int INDEX_MAJOR_FROM = 4;
   private static final int INDEX_MAJOR_TO = 7;
   private static final int INDEX_MINOR_FROM = 8;
   private static final int INDEX_MINOR_TO = 11;

   private static final int VERSION_LENGTH = 11;

   private String protocolString;

   /**
    * The class takes in a VNC reply string.
    * @param protocolString in the form "RFB 00X.00Y", where X is the major
    * version and Y is the minor one.
    */
   public VersionParser(String protocolString) {
      this.protocolString = protocolString;
   }

   /**
    * Parses the VNC string into an instance of object {@link ProtocolVersion}
    * @return {@link ProtocolVersion} a ProtocolVersion instance
    */
   public ProtocolVersion parse() {
      verifyInputIsValid(protocolString);
      int majorVersion = parseMajorVersion();
      int minorVersion = parseMinorVersion();
      return getSupportedVersion(majorVersion, minorVersion);
   }

   private int parseMinorVersion() {
      return parseSubstring(INDEX_MINOR_FROM, INDEX_MINOR_TO);
   }

   private int parseMajorVersion() {
      return parseSubstring(INDEX_MAJOR_FROM, INDEX_MAJOR_TO);
   }

   private int parseSubstring(int from, int to) {
      int version;
      try {
         String subString = protocolString.substring(from, to);
         version = Integer.parseInt(subString);
      } catch (NumberFormatException e) {
         final String errorMessage = "Invalid protocol version format: '%s'";
         String formattedMessage = String.format(errorMessage, protocolString);
         throw new IllegalArgumentException(formattedMessage);
      }
      return version;
   }

   private void verifyInputIsValid(String protocolString) {
      verifyNotNull(protocolString);
      verifyCorrectLength(protocolString);
   }

   private void verifyCorrectLength(String protocolString) {
      final int length = protocolString.length();
      if (length != VERSION_LENGTH) {
         final String error = "Unexpected version length: '%d'";
         final String errorMessage = String.format(error, length);
         throw new IllegalArgumentException(errorMessage);
      }
   }

   private void verifyNotNull(String protocolString) {
      if (protocolString == null) {
         throw new IllegalArgumentException("Protocol input is NULL.");
      }
   }

   private ProtocolVersion getSupportedVersion(int major, int minor) {
      ProtocolVersion version = getProtocolVersion(major, minor);
      verifyIsFound(version);
      return version;
   }

   private ProtocolVersion getProtocolVersion(int major, int minor) {
      ProtocolVersion protocolVersion = null;
      final ProtocolVersion[] supportedVersions = ProtocolVersion.values();
      for (ProtocolVersion supported : supportedVersions) {
         if (supported.getMajorVersion() == major &&
             supported.getMinorVersion() == minor) {
            protocolVersion = supported;
            break;
         }
      }
      return protocolVersion;
   }

   private void verifyIsFound(ProtocolVersion protocolVersion) {
      if (protocolVersion == null) {
         final String error = "Unrecognized protocol version: '%s'";
         final String errorMessage = String.format(error, this.protocolString);
         throw new UnrecognizedVersionException(errorMessage);
      }
   }
}
