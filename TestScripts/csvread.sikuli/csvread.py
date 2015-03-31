import csv
import os
csvFilePath = os.path.join(getBundlePath(), "csvdata.txt")
csvFile = csv.reader(open(csvFilePath))

Row = 3

def get(Row):    
  for i in range(Row+1):
    Row = csvFile.next()  
  print Row
  return Row

print get(Row)

exit()

mycsv = [] # empty list
for row in csvFile:
  mycsv.append(row)

print ("*********** rows - cols")
for nR in range(len(mycsv)):
    for nC in range(len(mycsv[nR])):
        print "Row: %d - Col: %d = %s"%(nR, nC, mycsv[nR][nC]) 

print ("*********** cols - rows")
mycsvCols = []
for nC in range(len(mycsv[0])):
    mycsvCols.append([])
    for nR in range(len(mycsv)):
        mycsvCols[nC].append(mycsv[nR][nC])
        print "Col: %d - Row: %d = %s"%(nC, nR, mycsvCols[nC][nR]) 
