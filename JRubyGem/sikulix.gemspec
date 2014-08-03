Gem::Specification.new do |s|
  s.name        = 'sikulix'
  s.version     = '1.1.0.1'
  s.date        = '2014-06-28'
  s.summary     = 'Sikulix gem'
  s.description = 'A wrapper over SikuliX java lib (sikulixapi.jar)'
  s.authors     = ['Roman S Samarev', 'Raimund Hocke']
  s.email       = 'rmhdevelop@me.com'
  s.files       = [
                   'sikulix.rb',
                   'sikulix/platform.rb',
                   'sikulix/sikulix.rb'
                  ].map {|f| 'lib/' + f}

  s.homepage    = 'http://sikulix.com'
  s.license     = 'MIT'
end
