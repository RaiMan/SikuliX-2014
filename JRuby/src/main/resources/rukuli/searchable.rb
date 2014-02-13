# The Rukuli::Searchable module is the heart of Sikuli. It defines the
# wrapper around Sikuli's on screen image searching and matching capability
# It is implemented by the Region class.
#
module Rukuli
  module Searchable

    # Public: search for an image within a Region
    #
    # filename   - A String representation of the filename to match against
    # similarity - A Float between 0 and 1 representing the threshold for
    # matching an image. Passing 1 corresponds to a 100% pixel for pixel
    # match. Defaults to 0.9 (90% match)
    #
    # Examples
    #
    #   region.find('needle.png')
    #   region.find('needle.png', 0.5)
    #
    # Returns an instance of Region representing the best match
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def find(filename, similarity = 0.9)
      begin
        pattern = build_pattern(filename, similarity)
        match = Region.new(@java_obj.find(pattern))
        match.highlight if Rukuli::Config.highlight_on_find
        match
      rescue NativeException => e
        raise_exception e, filename
      end
    end

    # Public: search for an image within a region (does not raise ImageNotFound exceptions)
    #
    # filename   - A String representation of the filename to match against
    # similarity - A Float between 0 and 1 representing the threshold for
    # matching an image. Passing 1 corresponds to a 100% pixel for pixel
    # match. Defaults to 0.9 (90% match)
    #
    # Examples
    #
    #   region.find!('needle.png')
    #   region.find!('needle.png', 0.5)
    #
    # Returns the match or nil if no match is found
    def find!(filename, similarity = 0.9)
      begin
        find(filename, similarity)
      rescue Rukuli::ImageNotFound => e
        nil
      end
    end

    # Public: search for an image within a Region and return all matches
    #
    # TODO: Sort return results so they are always returned in the same order
    # (top left to bottom right)
    #
    # filename   - A String representation of the filename to match against
    # similarity - A Float between 0 and 1 representing the threshold for
    # matching an image. Passing 1 corresponds to a 100% pixel for pixel
    # match. Defaults to 0.9 (90% match)
    #
    # Examples
    #
    #   region.find_all('needle.png')
    #   region.find_all('needle.png', 0.5)
    #
    # Returns an array of Region objects that match the given file and
    # threshold
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def find_all(filename, similarity = 0.9)
      begin
        pattern = build_pattern(filename, similarity)
        matches = @java_obj.findAll(pattern)
        regions = matches.collect do |r|
          match = Region.new(r)
          match.highlight if Rukuli::Config.highlight_on_find
          match
        end
        regions
      rescue NativeException => e
        raise_exception e, filename
      end
    end

    # Public: wait for a match to appear within a region
    #
    # filename   - A String representation of the filename to match against
    # time       - A Fixnum representing the amount of time to wait defaults
    # to 2 seconds
    # similarity - A Float between 0 and 1 representing the threshold for
    # matching an image. Passing 1 corresponds to a 100% pixel for pixel
    # match. Defaults to 0.9 (90% match)
    #
    # Examples
    #
    #    region.wait('needle.png') # wait for needle.png to appear for up to 1 second
    #    region.wait('needle.png', 10) # wait for needle.png to appear for 10 seconds
    #
    # Returns nothing
    #
    # Throws Rukuli::FileNotFound if the file could not be found on the system
    # Throws Rukuli::ImageNotMatched if no matches are found within the region
    def wait(filename, time = 2, similarity = 0.9)
      begin
        pattern = build_pattern(filename, similarity)
        match = Region.new(@java_obj.wait(pattern, time))
        match.highlight if Rukuli::Config.highlight_on_find
        match
      rescue NativeException => e
        raise_exception e, filename
      end
    end

    private

    # Private: builds a java Pattern to check
    #
    # filename   - A String representation of the filename to match against
    # similarity - A Float between 0 and 1 representing the threshold for
    # matching an image. Passing 1 corresponds to a 100% pixel for pixel
    # match. Defaults to 0.9 (90% match)
    #
    # Returns a org.sikuli.script::Pattern object to match against
    def build_pattern(filename, similarity)
      org.sikuli.script::Pattern.new(filename).similar(similarity)
    end
  end
end
