switchApp("Preview") # only for Mac testing with screenshots

# step 1: roughly define the grid area
head1 = find("LogsLogID.png")
head2 = head1.right().find("rightEnd.png")
head = head1.union(head2.left(1))
foot1 = find("btnOpen.png")
footline = Region(head.x, foot1.y, head.w, 1)
grid = footline.union(head.below(1))
#grid.highlight(2)

# step 2: adjust the gridarea for usage with Region grid feature
lineHeight = 17
grid.x += 3
grid.y += 2
lineCount = int(grid.h/lineHeight)
grid.h = lineCount*lineHeight
#grid.highlight(2)
# step 3: walk through the lines
grid.setRows(lineCount)
for i in range(lineCount):
  grid.getRow(i).highlight(1)
  break # uncomment to be faster

# read the textual content of the first 2 rows
Settings.OcrTextRead=True
import org.sikuli.natives.OCR as OCR
import org.sikuli.script.TextRecognizer as TR
TR.getInstance()

digits = "0123456789"
lower = "abcdefghijklmnopqrstuvwxyz"
upper = lower.upper()
chars = lower+upper
all = chars+digits

# read the result message
#OCR.setParameter("tessedit_char_whitelist", chars)
#OCR.setParameter("tessedit_char_whitelist", all)
print foot1.left(150).text()

OCR.setParameter("tessedit_char_whitelist", digits)
print "content of first row"
uprint(grid.getRow(0).text())

OCR.setParameter("tessedit_char_whitelist", "")
print "content of second row"
uprint(grid.getRow(1).text())

