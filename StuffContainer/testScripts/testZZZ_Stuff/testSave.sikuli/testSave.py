# -*- coding: utf-8 -*-

# https://bugs.launchpad.net/sikuli/+bug/1460616
# windows 7 64 bit, sikuli 1.1

import sys
from sikuli import *
from sikuli.Sikuli import *
sys.path.append(projectPath)

def testMessageEdit():
   assert()
   message21 = TableCell()
   FilterTable.clickMessage(message21)
   wait(2)

if __name__ == "__main__":
   testMessageEdit()

