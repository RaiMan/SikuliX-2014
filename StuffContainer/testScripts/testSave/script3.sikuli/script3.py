iePath = r'"C:\Program Files\Internet Explorer\iexplore.exe" '
ieLink = "http://germany.mfa.gov.ua/ua"
title = "powered by"

iePath = "C:\Python27\python.exe"

ie = App.open(iePath)
wait(3)
#click()
#wait(1)
#ie.focus()
#wait(2)
ie.close()

exit()

click()
wait(1)
ie = App.focus(title)
wait(2)

ie = App.open(iePath + ieLink)

wait(3)
click()
wait(2)

App.focus(title)
wait(2)

wait(3)
click()
wait(2)

ie.focus()
wait(2)

ie.close()