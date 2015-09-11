harfler = {u"ü": "0252",u"Ü": "0220",u"ı": "0305",u"İ": "0304",u"ç": "0231",u"Ç": "0199",
    u"ö": "0246",u"Ö": u"0214",u"ğ": "0287",u"Ğ": "0208",u"ş": "0351",u"Ş": "0350"}

def yaz(metin):
  global harfler
  for cMetin in metin:
    yazilacak = harfler.get(cMetin) #returns None, if not a character in harfler
    if not yazilacak:
      type(cMetin)
    else:
      strType ="keyDown(Key.ALT);"
      strType += "type(Key.NUM%s);"%yazilacak[0]
      strType += "type(Key.NUM%s);"%yazilacak[1]
      strType += "type(Key.NUM%s);"%yazilacak[2]
      strType += "type(Key.NUM%s);"%yazilacak[3]
      strType += "keyUp()"
      exec( strType)
yaz(u"all these üÜçÇöÖ are turkish characters")