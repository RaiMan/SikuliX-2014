import unittest

class UnitTestX(unittest.TestCase):

	def testBatchview(self):
		batchviewval = runScript("./testUT1")
		self.assertEqual(batchviewval, 1)

suite = unittest.TestLoader().loadTestsFromTestCase(UnitTestX)
unittest.TextTestRunner(verbosity=2).run(suite)