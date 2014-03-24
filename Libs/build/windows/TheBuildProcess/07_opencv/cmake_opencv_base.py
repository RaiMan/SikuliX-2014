file = open("cmake_opencv_base.txt")
out = open("cmake_opencv_base", "w")
out.write("cd build\n")
out.write("rm -f CMakeCache.txt\n")
out.write("make clean\n")
out.write("cmake -G \"MSYS Makefiles\" \\\n")
for line in file.readlines():
	line = line.strip()
	parm, val = line.split("=")
	parm = parm.split(":")[0]
	pout = "-D%s=\"%s\" \\"%(parm, val)
	print pout
	out.write(pout + "\n")
out.write("..\n")
out.close()