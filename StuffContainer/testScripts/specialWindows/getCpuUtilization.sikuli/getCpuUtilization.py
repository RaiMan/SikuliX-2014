cmd = "wmic path Win32_PerfFormattedData_PerfProc_Process get Name,PercentProcessorTime"
# yields output like:
# process_name cpu_utilization_value_in_%
# example   30
# example1  12

cmdOut = run(cmd) # issue command

# if 1 > cmdOut.count(RunTime.runCmdError):
# info = cmdOut.split(RunTime.NL)

info = cmdOut.split("\n") # get the output as list of lines
for line in info:
	line = line.strip() # get rid of suttounding whitespace
	if line.endswith(" 0"): continue # skip entries having value 0
	print line
  (name, cpu) = line.split() # get the name and percentage

