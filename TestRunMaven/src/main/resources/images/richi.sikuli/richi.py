def getInstructions(inp):
    store = {}
    try:
      configInput = open(inp)
      for line in configInput.readlines():
          (key, val) = line.strip().split("=")
          store[key.strip()] = val.strip()
      configInput.close()
    except: pass
    return store

def putInstructions(out, store):
    configOutput = open(out, "w")
    for key in store.keys():
        line = key + "=" + store[key] + "\n"
        configOutput.write(line)
    configOutput.close()

fInstructions = "/Users/rhocke/instructions.txt"

# step 1: fetch current instructions
instructions = getInstructions(fInstructions)
# step 2: modify instructions
instructions["data.configure.camera"] = "false"
# one might add additional instructions
instructions["data.configure.newoption"] = "true"
# step 3: write the instructions back to file
putInstructions(fInstructions, instructions)
