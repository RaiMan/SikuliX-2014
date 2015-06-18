print "***** starting testMac"
Debug.off()
bs = App("Safari")
bs.open()
wait(2)
print "open", bs.toStringShort()
je = App("jEdit")
je.open()
wait(2)
bs.focus()
print "focus", bs.toStringShort()
wait(2)
print bs
print je