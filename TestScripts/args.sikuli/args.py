# ./runsikulix.cmd -s
# curl http://localhost:50001/run/args?url=google.com&path=c:/program files (x86)/google/chrome/application/chrome.exe
import getopt, urllib

options, remainder = getopt.getopt(sys.argv[1:], '', ['url=', 'path='])

url, path = (None, None)

for k, v in options:
    if k in ('--url'):
        url = urllib.unquote(v)
    elif k in ('--path'):
        path = urllib.unquote(v)

if url is None and path is None:
	exit(1)

Debug.log('URL: ' + url)
Debug.log('Path: ' + path)

app = App(path)
app.open()

pid = app.getPID()

Debug.log('PID: ' + str(pid))

if (pid < 1):
	exit(1)

delay = 4
	
sleep(delay)
paste(url)
sleep(delay)
type(Key.ENTER)
sleep(delay)
app.close()
sleep(delay)

exit(0)