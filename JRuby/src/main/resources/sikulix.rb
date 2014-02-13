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
