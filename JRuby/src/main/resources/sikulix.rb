# SikuliX

require 'java'

module SikuliX4Ruby

  java_import org.sikuli.basics.SikuliX
  java_import org.sikuli.script.Screen
  java_import org.sikuli.script.Region
  java_import org.sikuli.script.compare.DistanceComparator
  java_import org.sikuli.script.Observer

  $SIKULI_SCREEN = Screen.new


  UNDOTTED_METHODS =
    [SikuliX, $SIKULI_SCREEN].inject({}) do |h, obj|
      p obj
      h.merge!(obj.methods.inject({}){|h, name| h.merge!(name => obj.method(name))})
    end

  def method_missing name, *args
    ret = nil
    puts "method not exists: #{name} - trying SikuliX"
    begin
      puts "using: #{args}"
      if method = UNDOTTED_METHODS[name] then
        puts "SikuliX has: #{name}"
        ret = method.call *args
        Object.send(:define_method, name){ |*args| method.call *args }
        return ret
      else
        raise "Problem (#{e}) with SikuliX.#{m} (#{args})"
      end
    rescue Exception => e
      raise "Problem (#{e}) with SikuliX.#{m} (#{args})"
    end
  end


  class Region
    def on_vanish target, &block
      onVanish target, block
    end
  end

  # Public: the setter for the absolute file path where Sikuli will search
  # for images with given a filename as an image
  #
  # Examples
  #
  #  Rukuli::Config.image_path = "/Users/andreanastacio/rukuli/images/"
  #
  # Returns nothing
  def image_path=(path)
    java.lang.System.setProperty("SIKULI_IMAGE_PATH", path)
  end

  # Public: turns stdout logging on and off for the Sikuli java classes.
  # Defaults to true.
  #
  # Examples
  #
  #  Rukuli::Config.logging = false
  #
  # Returns nothing
  def logging=(boolean)
    return unless [TrueClass, FalseClass].include? boolean.class
    org.sikuli.basics::Settings.InfoLogs   = boolean
    org.sikuli.basics::Settings.ActionLogs = boolean
    org.sikuli.basics::Settings.DebugLogs  = boolean
  end
end

=begin Rukuli variant

require_relative 'rukuli'
include Rukuli

$screen = Screen.new

clickable  = [:click, :double_click, :click_and_hold, :drag_drop,
              :hover, :wheel_down, :wheel_up ]

typeable   = [:enter, :type]

searchable = [:find, :find!, :find_all, :wait]

(clickable + typeable + searchable).each do |name|
  method = $screen.method (name)
  Object.send(:define_method, name){ |*args| method.call *args }
end
=end