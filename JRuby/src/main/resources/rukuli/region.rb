# A Region represents a rectangle on screen. Regions are the main point of
# interaction for Sikuli actions. Regions can receive actions from the mouse,
# keyboard, and image search.
#
require "rukuli/clickable"
require "rukuli/typeable"
require "rukuli/searchable"

module Rukuli
  class Region
    include Clickable
    include Typeable
    include Searchable

    # Public: creates a new Region object
    #
    # args - Array representing x (left bound), y (top), width, height
    #        4 Fixnums left, top, width, height
    #        An instance of an org.sikuli.script::Region
    #
    #   Examples
    #
    #     Region.new([10, 10, 200, 300])
    #     Region.new(10, 10, 200, 300)
    #     Region.new(another_region)
    #
    # Returns the newly initialized object
    def initialize(*args)
      @java_obj = org.sikuli.script::Region.new(*args)
    end

    # Public: highlight the region with a ~ 5 pixel red border
    #
    # seconds - Fixnum length of time to show border
    #
    # Returns nothing
    def highlight(seconds = 1)
      @java_obj.java_send(:highlight, [Java::int], seconds)
    end

    # Public: the x component of the top, left corner of the Region
    def x
      @java_obj.x()
    end

    # Public: the y component of the top, left corner of the Region
    def y
      @java_obj.y()
    end

    # Public: the width in pixels of the Region
    def width
      @java_obj.w()
    end

    # Public: the height in pixels of the Region
    def height
      @java_obj.h()
    end

    # Public: provide access to all region methods provided by the SikuliScript API
    # See http://sikuli.org/doc/java/edu/mit/csail/uid/Region.html
    def method_missing method_name, *args, &block
      @java_obj.send method_name, *args, &block
    end

    private

    # Private: interpret a java NativeException and raises a more descriptive
    # exception
    #
    # exception - The original java exception thrown by the sikuli java_obj
    # filename  - A string representing the filename to include in the
    # exception message
    #
    # Returns nothing
    def raise_exception(exception, filename)
      message = exception.message
      if message.start_with? "java.lang."
        raise exception.message
      elsif message.start_with? "org.sikuli.script.FindFailed"
        raise Rukuli::FileDoesNotExist, "The file '#{filename}' does not exist."
      else
        raise Rukuli::ImageNotFound, "The image '#{filename}' did not match in this region."
      end
    end
  end
end
