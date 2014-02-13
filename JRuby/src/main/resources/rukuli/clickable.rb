# The Clickable module defines interaction with the mouse. It is included in
# the Region class.
#
module Rukuli
  module Clickable

    # Public: Performs a single click on an image match or point (x, y)
    #
    # args - String representing filename of image to find and click
    # args - Fixnum, Fixnum representing x and y coordinates within
    # a Region (0,0) is the top left
    #
    # Examples
    #
    #   region.click('smile.png')
    #   region.click(123, 432)
    #
    # Returns nothing
    def click(*args)
      case args.length
        when 1 then click_image(args[0])
        when 2 then click_point(args[0], args[1])
        else raise ArgumentError
      end
    end

    # Public: Performs a double click on an image match or point (x, y)
    #
    # args - String representing filename of image to find and click
    # args - Fixnum, Fixnum representing x and y coordinates within
    # a Region (0,0) is the top left
    #
    # Examples
    #
    #   region.double_click('smile.png')
    #   region.double_click(123, 432)
    #
    # Returns nothing
    def double_click(*args)
      case args.length
        when 1 then click_image(args[0], true)
        when 2 then click_point(args[0], args[1], true)
        else raise ArgumentError
      end
    end

    # Public: Performs a click and hold on an image match or point (x, y)
    #
    # args - String representing filename of image to find and click
    # args - Fixnum, Fixnum representing x and y coordinates within
    # a Region (0,0) is the top left
    # seconds - Fixnum representing the number of seconds to hold down
    # before releasing
    #
    # Examples
    #
    #   region.click_and_hold('smile.png', 2)
    #   region.click_and_hold(123, 432, 2)
    #
    # Returns nothing
    def click_and_hold(seconds = 1, *args)
      case args.length
        when 1 then click_image_and_hold(args[0], seconds)
        when 2 then click_point_and_hold(args[0], args[1], seconds)
        else raise ArgumentError
      end
    end

    # Public: Performs a mouse down, drag, and mouse up
    #
    # start_x - Fixnum representing the x of the mouse down
    # start_y - Fixnum representing the y of the mouse down
    # end_x   - Fixnum representing the x of the mouse up
    # end_y   - Fixnum representing the y of the mouse up
    #
    # Examples
    #
    #   region.drag_drop(20, 12, 23, 44)
    #
    # Returns nothing
    def drag_drop(start_x, start_y, end_x, end_y)
      @java_obj.dragDrop(
        offset_location(start_x, start_y),
        offset_location(end_x, end_y)
      )
    end

    # Public: Simulates turning of the mouse wheel up
    #
    # steps - Fixnum representing the number of steps to turn the mouse wheel
    #
    # Examples
    #
    #   region.wheel_up(10)
    #
    # Returns nothing
    def wheel_up(steps = 1)
      @java_obj.wheel(-1, steps)
    end

    # Public: Simulates turning of the mouse wheel down
    #
    # steps - Fixnum representing the number of steps to turn the mouse wheel
    #
    # Examples
    #
    #   region.wheel_down(10)
    #
    # Returns nothing
    def wheel_down(steps = 1)
      @java_obj.wheel(1, steps)
    end

    # Public: Performs a hover on an image match or point (x, y)
    #
    # args - String representing filename of image to find and hover
    # args - Fixnum, Fixnum representing x and y coordinates within
    # a Region (0,0) is the top left
    #
    # Examples
    #
    #   region.hover('smile.png')
    #   region.hover(123, 432)
    #
    # Returns nothing
    def hover(*args)
      case args.length
        when 1 then hover_image(args[0])
        when 2 then hover_point(args[0], args[1])
        else raise ArgumentError
      end
    end

    private

    # Private: turns the mouse wheel
    #
    # direction - Fixnum represeting direction to turn wheel
    # steps - the number of steps to turn the mouse wheel
    #
    # Returns nothing
    def wheel(direction, steps)
      @java_obj.wheel(direction, steps)
    end

    # Private: clicks on a matched Region based on an image based search
    #
    # filename - A String representation of the filename of the region to
    # match against
    # seconds  - The length in seconds to hold the mouse
    #
    # Returns nothing
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def click_image_and_hold(filename, seconds)
      begin
        pattern = org.sikuli.script::Pattern.new(filename).similar(0.9)
        @java_obj.hover(pattern)
        @java_obj.mouseDown(java.awt.event.InputEvent::BUTTON1_MASK)
        sleep(seconds.to_i)
        @java_obj.mouseUp(0)
      rescue NativeException => e
        raise_exception e, filename
      end
    end

    # Private: clicks on a point within the region
    #
    # filename - A String representation of the filename of the region to
    # match against
    #
    # Returns nothing
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def click_point_and_hold(x, y, seconds)
      begin
        @java_obj.hover(location(x, y))
        @java_obj.mouseDown(java.awt.event.InputEvent::BUTTON1_MASK)
        sleep(seconds.to_i)
        @java_obj.mouseUp(0)
      rescue NativeException => e
        raise_exception e, filename
      end
    end

    # Private: clicks on a matched Region based on an image based search
    #
    # filename - A String representation of the filename of the region to
    # match against
    # is_double - (optional) Boolean determining if should be a double click
    #
    # Returns nothing
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def click_image(filename, is_double = false, and_hold = false)
      begin
        if is_double
          @java_obj.doubleClick(filename, 0)
        else
          @java_obj.click(filename, 0)
        end
      rescue NativeException => e
        raise_exception e, filename
      end
    end

    # Private: clicks on a point relative to a Region's top left corner
    #
    # x         - a Fixnum representing the x component of the point to click
    # y         - a Fixnum representing the y component of the point to click
    # is_double - (optional) Boolean determining if should be a double click
    #
    # Returns nothing
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def click_point(x, y, is_double = false)
      if is_double
        @java_obj.doubleClick(offset_location(x, y))
      else
        @java_obj.click(offset_location(x, y))
      end
    end

    # Private: hovers on a matched Region based on an image based search
    #
    # filename - A String representation of the filename of the region to
    # match against
    #
    # Returns nothing
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def hover_image(filename)
      begin
        @java_obj.hover(filename)
      rescue NativeException => e
        raise_exception e, filename
      end
    end

    # Private: hovers on a point relative to a Region's top left corner
    #
    # x         - a Fixnum representing the x component of the point to hover
    # y         - a Fixnum representing the y component of the point to hover
    #
    # Returns nothing
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def hover_point(x, y)
      @java_obj.hover(offset_location(x, y))
    end

    # Private: create a new instance of Location
    #
    # x         - a Fixnum representing the x component of the point to hover
    # y         - a Fixnum representing the y component of the point to hover
    #
    # Return location class instance
    def location(x, y)
      org.sikuli.script::Location.new(x, y)
    end

    # Private: location with offset
    #
    # x         - a Fixnum representing the x component of the point to hover
    # y         - a Fixnum representing the y component of the point to hover
    #
    # Return new location
    def offset_location(x, y)
      location(x, y).offset(x(), y())
    end
  end
end
