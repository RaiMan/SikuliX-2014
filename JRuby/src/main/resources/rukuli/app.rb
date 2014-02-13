# An App object represents a running app on the system.
#
module Rukuli
  class App

    # Public: creates a new App instance
    #
    # app_name - String name of the app
    #
    # Examples
    #
    #   App.new("TextEdit")
    #
    # Returns the newly initialized App
    def initialize(app_name)
      @java_obj = org.sikuli.script::App.new(app_name)
    end

    # Public: brings the App to focus
    #
    # Returns nothing
    def focus
      @java_obj.focus()
    end

    # Public: the Region instance representing the app's window
    #
    # Returns the newly initialized Region
    def window
      Region.new(@java_obj.window())
    end
  end
end
