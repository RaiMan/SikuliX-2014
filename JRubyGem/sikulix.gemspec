Gem::Specification.new do |s|
  s.name        = 'sikulix'
  s.version     = '1.1.0.1'
  s.date        = '2012-06-28'
  s.summary     = 'SikuliX gem'
  s.description = 'This is a wrapper over SikuliX java lib'
  s.authors     = ['Roman S Samarev']
  s.email       = 'samarev@acm.org'
  s.files       = [
                   'sikulix.rb',
                   'sikulix/platform.rb',
                   'sikulix/sikulix.rb'
                  ].map {|f| 'lib/' + f}

  s.homepage    = 'http://sikulix.com'
  s.license     = 'MIT'
end
