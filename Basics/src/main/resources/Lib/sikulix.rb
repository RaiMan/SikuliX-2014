# SikuliX for Ruby
require 'java'

# Classes and methods for using SikuliX
module SikuliX4Ruby

  java_import org.sikuli.basics.SikuliX
  java_import org.sikuli.script.Screen
  java_import org.sikuli.script.Region
  java_import org.sikuli.script.ScreenUnion

  java_import org.sikuli.script.Observing
  java_import org.sikuli.script.ObserverCallBack

  java_import org.sikuli.script.Constants
  java_import org.sikuli.script.Finder

  java_import org.sikuli.script.Button
  java_import org.sikuli.basics.OS

  java_import org.sikuli.script.Match
  java_import org.sikuli.script.Pattern
  java_import org.sikuli.script.Location

  java_import org.sikuli.script.ImagePath

  java_import org.sikuli.script.App
  java_import org.sikuli.script.Key
  java_import org.sikuli.script.KeyModifier
  java_import org.sikuli.script.Mouse

  java_import org.sikuli.basics.Settings
  java_import org.sikuli.basics.ExtensionManager

  java_import org.sikuli.script.compare.DistanceComparator
  java_import org.sikuli.script.compare.VerticalComparator
  java_import org.sikuli.script.compare.HorizontalComparator

  java_import org.sikuli.basics.SikuliScript

  java_import org.sikuli.basics.Debug

  #
  # This method generates a wrapper for Java Native exception processing
  # in native java methods. It allows to detect a line number in script
  # that opened in IDE where the exception was appearing.
  #
  # obj - class for the wrapping
  # methods_array - array of method names as Symbols
  def self.native_exception_protect( obj, methods_array )
    methods_array.each do |name|
      m = obj.instance_method (name)
      obj.class_exec do
        alias_method ('java_' + name.to_s).to_sym, name
        define_method(name) do |*args|
          begin
            # java specific call for unbound methods
            m.bind(self).call *args
          rescue NativeException => e;
            raise StandardError.new(e.message);
          end
        end
      end
    end
  end

  # Redefinition of native org.sikuli.script.Region class
  class Region
    # Service class for all callbacks processing
    class RObserverCallBack < ObserverCallBack
      def initialize(block); super(); @block=block; end;
      %w(appeared vanished changed).each do |name|
        define_method(name) do |*args|
          @block.call *(args.first @block.arity)
        end
      end
    end
    alias_method :java_onAppear, :onAppear
    alias_method :java_onVanish, :onVanish
    alias_method :java_onChange, :onChange

    # Redefinition of the java method for Ruby specific
    def onAppear target, &block
      java_onAppear(target, RObserverCallBack.new(block))
    end

    # Redefinition of the java method for Ruby specific
    def onVanish target, &block
      java_onVanish(target, RObserverCallBack.new(block))
    end

    # Redefinition of the java method for Ruby specific
    def onChange &block
      java_onChange(RObserverCallBack.new(block))
    end

    #alias_method :java_findAll,  :findAll
    #def findAll *args
    #  begin
    #    java_findAll *args
    #  rescue NativeException => e; raise e.message; end
    #end
  end

  # Wrap following java-methods by an exception processor
  native_exception_protect Region,
    [:find, :findAll, :wait, :waitVanish, :exists,
     :click, :doubleClick, :rightClick, :hover, :dragDrop,
     :type, :paste, :observe]

  # default screen object for "undotted" methods
  $SIKULI_SCREEN = Screen.new

  # generate hash of (method name => method) for all possible "undotted" methods
  UNDOTTED_METHODS =
    [$SIKULI_SCREEN, SikuliX].inject({}) do |h, obj|
      h.merge!(obj.methods.inject({}){|h, name| h.merge!(name => obj.method(name))})
    end

  # display some help in interactive mode
  def shelp
    SikuliScript.shelp
  end

end

# This method allow to call "undotted" methods that belong to
# Region/Screen or SikuliX classes.
def self.method_missing name, *args, &block
  begin
    Debug.log 3, "SikuliX4Ruby: looking for undotted method: #{name}"
    if method = SikuliX4Ruby::UNDOTTED_METHODS[name] then
      ret = method.call *args, &block
      # Dynamic methods that throw a native Java-exception,
      # hide a line number in the scriptfile!
      #Object.send(:define_method, name){ |*args| method.call *args }
      return ret
    else
      raise "undotted method '#{name}' missing"
    end
  rescue NativeException => e
    raise "SikuliX4Ruby: Problem (#{e})\nwith undotted method: #{name} (#{args})"
  end
end
