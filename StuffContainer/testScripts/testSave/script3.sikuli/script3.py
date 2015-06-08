iePath = r'"C:\Program Files\Internet Explorer\iexplore.exe" '
ieLink = "http://germany.mfa.gov.ua/ua"
title = ucode("Посольство України")
title = "powered by"

click()
wait(1)
ie = App.focus(title)
wait(2)
#print ie
exit()

ie = App.open(iePath + ieLink)

wait(3)
click()
wait(2)

App.focus("SikuliX powered")
wait(2)


wait(3)
click()
wait(2)

ie.focus()
wait(2)
