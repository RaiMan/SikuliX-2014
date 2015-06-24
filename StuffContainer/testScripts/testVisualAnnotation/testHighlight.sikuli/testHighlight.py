#import org.sikuli.util.ScreenHighlighter as SH
Region(100, 100, 100, 100).highlight(); wait(1)
popup("ok")
Region(100, 300, 100, 100).highlight(); wait(1)
popup("ok2")
#SH.closeAll()
highlightOff()
wait(3)
