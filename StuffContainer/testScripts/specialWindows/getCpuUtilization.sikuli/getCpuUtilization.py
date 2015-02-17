cmd = "wmic path Win32_PerfFormattedData_PerfProc_Process get Name,PercentProcessorTime"
cmdOut = run(cmd)
if 1 > cmdOut.count(RunTime.runCmdError):
  info = cmdOut.split(RunTime.NL)
  for line in info:
    line = line.strip()
    if not line.endswith(" 0"):
      print line.strip()

