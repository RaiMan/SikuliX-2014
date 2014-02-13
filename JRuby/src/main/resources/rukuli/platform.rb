module Rukuli
  class Platform

    def self.sikulix_path
      path = "#{ENV['SIKULIX_HOME']}"
      if ENV['SIKULIX_HOME'].nil?
        raise LoadError, "Failed to load 'sikuli-java.jar'\nMake sure SIKULIX_HOME is set!"
      end
      path
    end
  end
end
