import org.sikuli.script.TextRecognizer as TR
Settings.OcrReadText = True
Settings.OcrLanguage = "chi_sim"
TR.reset()
print selectRegion().text()