Debug.off()

# use the tasklist command to get details about the app
start = time.time()
cmd = 'tasklist /V /FO CSV /NH /FI "SESSIONNAME eq Console"'
result = run(cmd)
temp = result.split("\r\n")

info = []
for n in range(len(temp)):
  if n == 0: continue
  e = temp[n].strip()
  if e == "": continue
  print e
  detail = []
  for val in e.split('"'):
    if val == ",": continue
    if val == "": continue
    #print val
    detail.append(val)
  if detail[-1] == "N/A": continue
  if detail[6] == "N/A": continue
  info.append([detail[0], int(detail[1]), detail[-1]])  
print time.time() - start
for e in info:
  print e
exit()
# evaluate the info
info = []
ni = 0
for n in range(len(temp)):
  if n == 0: continue # skip the return code 
  e = temp[n].strip()
  if e == "": continue # skip empty lines
  parts = e.split(":") # get key/value
  info.append(parts[1].strip()) #store value
  #print parts[0], "(%d) ="%ni, info[-1] # for debugging
  ni += 1
  
appNP.close() # close the app

# show some app info
print 'app: %s (pid: %s) windowtitle: "%s"' % (info[0], info[1], info[-1])

"""
************** the output produced by this example using the debug prints
Notepad PID: 8480
[info] runcmd: tasklist /v /FO LIST /FI "IMAGENAME eq notepad++.exe" 
0
Image Name:   notepad++.exe
PID:          8480
Session Name: Console
Session#:     1
Mem Usage:    21,452 K
Status:       Running
User Name:    RAIMAN-PC\RaiMan
CPU Time:     0:00:00
Window Title: new  0 - Notepad++
Image Name (0) = notepad++.exe
PID (1) = 8480
Session Name (2) = Console
Session# (3) = 1
Mem Usage (4) = 21,452 K
Status (5) = Running
User Name (6) = RAIMAN-PC\RaiMan
CPU Time (7) = 0
Window Title (8) = new  0 - Notepad++
app: notepad++.exe (pid: 8480) windowtitle: "new  0 - Notepad++"
"""