import codecs
import java.lang.System as JS

#sys.stdin  = codecs.getreader('utf-8')(sys.stdin)
#sys.stdout = codecs.getwriter('utf-8')(sys.stdout)
text = u'\u6f22'
text = u'\u5b57'
text = u'\u0409'
text = "漢字"
text = "漢" 
text = u"字"
textu = unicd(text)
JS.out.println("plainJ: " + text)
JS.out.println("unicodeJ: " + textu)
popup("plain: " + text)
popup("unicode: " + textu)
#print "plain:", text
print "unicode:" 
uprint (unicode(text))
exit()
text = "漢字"
text = u'\u4e00'
text = u'\u6f22'
text = u'\u0409'
popup(text)
popup( unicd(text))
JS.out.println(unicode(text,"utf-8"))

