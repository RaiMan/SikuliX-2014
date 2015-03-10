popup("Hello from Git showcase Ruby")

java_import java.awt.Desktop
java_import java.net.URI

if (not Desktop.isDesktopSupported())
	exit(1)
end

dt = Desktop.getDesktop()
dt.browse(URI.new("http://sikulix.com"))

r = wait("sxpower", 5).below(100)
r.click("quickstart")
wait(3.0)

if ($RUNTIME.runningMac)
  write("#M.w")
else
  write("#C.w")
end
