import xlrd
print "*** running xlrd version: ", xlrd.__VERSION__
fXLS = "/Users/rhocke/SikuliX/Python/XLxx-Suite/test.xls"
print "*** trying to open: ", fXLS 

print xlrd.open_workbook(fXLS)

# xlrd.dump(fXLS)

