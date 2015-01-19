# coding: utf-8
# SikuliX for Ruby

require 'java'

# Classes and methods for using SikuliX
module Sikulix
  private

  # 'private' for avoiding of unexpected effects when
  #   'include Sikulix' is used.
  java_import java.net.Socket
  java_import 'edu.unh.iol.dlc.ConnectionController'
  java_import org.sikuli.script.Sikulix
  java_import org.sikuli.script.Screen
  java_import org.sikuli.script.Region
  java_import org.sikuli.script.ScreenUnion

  java_import org.sikuli.script.Observing
  java_import org.sikuli.script.ObserverCallBack

  java_import org.sikuli.script.Constants
  java_import org.sikuli.script.Finder
  java_import org.sikuli.script.ImageFinder
  java_import org.sikuli.script.ImageFind

  java_import org.sikuli.script.Button
  java_import org.sikuli.basics.OS

  java_import org.sikuli.script.Match
  java_import org.sikuli.script.Pattern
  java_import org.sikuli.script.Location

  java_import org.sikuli.script.ImagePath
  java_import org.sikuli.script.Image
  java_import org.sikuli.script.ImageGroup

  java_import org.sikuli.script.App
  java_import org.sikuli.script.Key
  java_import org.sikuli.script.KeyModifier
  java_import org.sikuli.script.Mouse
  java_import org.sikuli.script.Keys

  java_import org.sikuli.basics.Settings
  java_import org.sikuli.basics.ExtensionManager

  java_import org.sikuli.script.compare.DistanceComparator
  java_import org.sikuli.script.compare.VerticalComparator
  java_import org.sikuli.script.compare.HorizontalComparator

  java_import org.sikuli.basics.Debug

	begin
	  java_import org.sikuli.scriptrunner.ScriptRunner
	rescue
  end

  #
  # This method generates a wrapper for Java Native exception processing
  # in native java methods. It allows to detect a line number in script
  # that opened in IDE where the exception was appearing.
  #
  # obj - class for the wrapping
  # methods_array - array of method names as Symbols
  def self.native_exception_protect(obj, methods_array)
    methods_array.each do |name|
      m = obj.instance_method name
      obj.class_exec do
        alias_method(('java_' + name.to_s).to_sym, name)
        define_method(name) do |*args|
          begin
            # java specific call for unbound methods
            m.bind(self).call(*args)
          rescue NativeException => e
            raise StandardError, e.message
          end
        end
      end
    end
  end

  # Dynamic method definition for several cases of invocation
  def self.dynamic_def(name, &block)
    # using as:
    #   include Sikulix
    #   some_method(...)
    define_method(name, &block)
    # using as:
    #   Sikulix::some_method(...)
    define_singleton_method(name, &block)

    # private method to avoid of attachment to all subclasses
    private name
  end

  # Redefinition of native org.sikuli.script.Region class
  class Region
    # Service class for all callbacks processing
    class RObserverCallBack < ObserverCallBack # :nodoc: all
      def initialize(block)
        super()
        @block = block
      end
      %w(appeared vanished changed).each do |name|
        define_method(name) do |*args|
          @block.call(*(args.first @block.arity))
        end
      end
    end
    alias_method :java_onAppear, :onAppear
    alias_method :java_onVanish, :onVanish
    alias_method :java_onChange, :onChange

    # Redefinition of the java method for Ruby specific
    def onAppear(target, &block)
      java_onAppear target, RObserverCallBack.new(block)
    end

    # Redefinition of the java method for Ruby specific
    def onVanish(target, &block)
      java_onVanish target, RObserverCallBack.new(block)
    end

    # Redefinition of the java method for Ruby specific
    def onChange(&block)
      java_onChange RObserverCallBack.new(block)
    end

    # alias_method :java_findAll,  :findAll
    # def findAll(*args)
    #   begin
    #     java_findAll(*args)
    #   rescue NativeException => e; raise e.message; end
    # end
  end

  # Wrap following java-methods by an exception processor
  native_exception_protect(
    Region,
    [:find, :findAll, :wait, :waitVanish, :exists,
     :click, :doubleClick, :rightClick, :hover, :dragDrop,
     :type, :paste, :observe]
   )

  # Default screen object for "undotted" methods.
  $SIKULI_SCREEN = Screen.new
  $DEFAULT_SCREEN = $SIKULI_SCREEN
  $VNC_SCREEN_POOL = []
  $CONNECTION_CONTROLLER = false

  def initVNCPool(*args)
    if $CONNECTION_CONTROLLER
      puts "VNC Pool already initialized, free it first!"
      return
    end
    sockets = []
    args.each do |str|
      address = str.scan(/[0-9\.]+/)[0]
      port = str.scan(/[0-9\.]+/)[1].to_i
      s = Socket.new(address, port)
      s.setSoTimeout(1000)
      s.setKeepAlive(true)
      sockets << s
    end    
    $CONNECTION_CONTROLLER = ConnectionController.new(*sockets)
    cc = $CONNECTION_CONTROLLER
    sockets.size.times do |id|
      cc.openConnection(id)
      cc.setPixelFormat(id, "Truecolor", 32, 0)
      cc.start(id)
    end
    sleep 2
    java_import 'edu.unh.iol.dlc.VNCScreen'
    sockets.size.times do |id|
      $VNC_SCREEN_POOL << VNCScreen.new(id)
    end
    puts "Pool of #{$SCREEN_POOL} vnc connections initialized"
  end
  
  def setDefaultScreen(screen)
    if screen.respond_to?(:click)
      $DEFAULT_SCREEN = screen
      puts "Screen switched"
    end
  end

  def freeVNCPool
    $VNC_SCREEN_POOL.size.times { $CONNECTION_CONTROLLER.close_connection(0) }
    $CONNECTION_CONTROLLER = false
  end

# This is an alternative for method generation using define_method
#  # Generate hash of ('method name'=>method)
#  # for all possible "undotted" methods.
#  UNDOTTED_METHODS =
#    [$SIKULI_SCREEN, Sikulix].reduce({}) do |h, obj|
#      h.merge!(
#        obj.methods.reduce({}) do |h2, name|
#          h2.merge!(name => obj.method(name))
#        end
#      )
#    end

  # It makes possible to use java-constants as a methods
  # Example: Key.CTRL instead of Key::CTRL
  [Key, KeyModifier].each do |obj|
    obj.class_exec do
      def self.method_missing(name)
        if (val = const_get(name))
          return val
        end
        fails "method missing #{name}"
      end
    end
  end

  # Generate static methods in Sikulix context
  # for possible "undotted" methods.
  Sikulix.java_class.method(:java_class_methods).call.map(&:name).uniq.each do |name|
    obj_meth = Sikulix.method(name)
    dynamic_def(name) { |*args, &block| obj_method.call(*args, &block) }
  end

  $SIKULI_SCREEN.java_class.method(:java_instance_methods).call.map(&:name).uniq.each do |name|
    dynamic_def(name) { |*args, &block| $DEFAULT_SCREEN.method(name).call(*args, &block) }
  end

  # TODO: check it after Env Java-class refactoring
  java_import org.sikuli.script.Env
  java_import org.sikuli.basics.HotkeyListener

  class Env  # :nodoc: all
    class RHotkeyListener < HotkeyListener
      def initialize(block)
        super()
        @block = block
      end

      def hotkeyPressed(event)
        @block.call event
      end
    end
  end

  ##
  # Register hotkeys
  #
  # Example:
  #    addHotkey( Key::F1, KeyModifier::ALT + KeyModifier::CTRL ) do
  #      popup 'hallo', 'Title'
  #    end
  #
  def addHotkey(key, modifiers, &block)
    Env.addHotkey key, modifiers, Env::RHotkeyListener.new(block)
  end

  ##
  # Unregister hotkeys
  #
  # Example:
  #    removeHotkey( Key::F1, KeyModifier::ALT + KeyModifier::CTRL )
  def removeHotkey(key, modifiers)
    Env.removeHotkey key, modifiers
  end

  # Generate methods like constructors.
  # Example: Pattern("123.png").similar(0.5)
  [Pattern, Region, Screen, App].each do |cl|
    name = cl.java_class.simple_name
    dynamic_def(name) { |*args| cl.new(*args) }
  end
  dynamic_def("Location") { |*args| Location.new(*args).setOtherScreen($DEFAULT_SCREEN) }

end

## This method allow to call "undotted" methods that belong to
## Region/Screen or SikuliX classes.
# def self.method_missing(name, *args, &block)
#
#  if (method = Sikulix::UNDOTTED_METHODS[name])
#    begin
#      ret = method.call(*args, &block)
#      # Dynamic methods that throw a native Java-exception,
#      # hide a line number in the scriptfile!
#      # Object.send(:define_method, name){ |*args| method.call(*args) }
#      return ret
#    rescue NativeException => e
#      raise StandardError, "Sikulix: Problem (#{e})\n" \
#        "with undotted method: #{name} (#{args})"
#    end
#  else
#    fail "undotted method '#{name}' missing"
#  end
# end
