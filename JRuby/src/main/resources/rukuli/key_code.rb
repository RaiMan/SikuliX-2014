require 'java'
java_import 'org.sikuli.script.Key'
java_import 'org.sikuli.script.KeyModifier'

#
# These constants represent keyboard codes for interacting with the keyboard.
# Keyboard interaction is defined in the Rukuli::Typeable module.
#
module Rukuli
  KEY_CMD   = KeyModifier::META
  KEY_SHIFT = KeyModifier::SHIFT
  KEY_CTRL  = KeyModifier::CTRL
  KEY_ALT   = KeyModifier::ALT
  
  KEY_BACKSPACE = Key::BACKSPACE
  KEY_RETURN    = Key::ENTER
  LEFT_ARROW    = Key::LEFT
  RIGHT_ARROW   = Key::RIGHT
  UP_ARROW      = Key::UP
  DOWN_ARROW    = Key::DOWN
end
