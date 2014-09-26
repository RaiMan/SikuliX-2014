#Sikulix.prefStore("test1", "some text1")
#Sikulix.prefStore("test2", "some text2")
#Sikulix.prefStore("test3", "some text3")

#Sikulix.prefRemove();

for i in range(1,4):
  print Sikulix.prefLoad("test" + str(i), "not set")

#print Sikulix.prefRemove("test1")
