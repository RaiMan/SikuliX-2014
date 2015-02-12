Debug.off()
switchApp("OpenOffice")

# Move to top left cell
# write("#W1.#M.#L.#M.#U.#W1.")
wait(1)
type(Key.LEFT, Key.CMD)
type(Key.LEFT, Key.CMD)
wait(1)
for i in range(3):
  #write("#M.c")
  type("c", Key.CMD)
  print Env.getClipboard()
  #write("#R.#W1.")
  type(Key.RIGHT)
  wait(1)