# A Screen object defines a special type of Rukuli::Region that represents
# the entire screen.
#
# TODO: Test the Screen object with multiple monitors attached.
#
module Rukuli
  class Screen < Region

    # Public: creates a new Screen object
    #
    # Examples
    #
    #   screen = Rukuli::Screen.new
    #
    # Returns the newly initialized Screen object
    def initialize
      @java_obj = org.sikuli.script::Screen.new()
    end
  end
end
