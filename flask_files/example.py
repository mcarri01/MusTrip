import os
import testapp
import unittest
import random

class testappTestCase(unittest.TestCase):

    def setUp(self):
        self.app = testapp.app.test_client()

    # Addition
    def testAdd(self):
        for _ in range(100):
            rand1 = random.randint(1, 100)
            rand2 = random.randint(1, 100)
            res = self.app.post("/add", data=dict(
                num1=rand1,
                num2=rand2
            ))
            data = res.get_data()
            self.assertEqual(int(data), rand1 + rand2)

    # Multiplication
    def testMult(self):
        for _ in range(100):
            rand1 = random.randint(1, 100)
            rand2 = random.randint(1, 100)
            res = self.app.post("/multiple", data=dict(
                num1=rand1,
                num2=rand2
            ))
            data = res.get_data()
            self.assertEqual(int(data), rand1 * rand2)
            
    # Division
    def testDiv(self):
        for _ in range(100):
            rand1 = random.randint(1, 100)
            rand2 = random.randint(1, 100)
            res = self.app.post("/divide", data=dict(
                num1=rand1,
                num2=rand2
            ))
            data = res.get_data()
            self.assertEqual(int(data), rand1 / rand2)
    
    def testMistake(self):
        res = self.app.post("/add", data=dict(
            num1=1,
            num2=1
        ))
        data = res.get_data()
        self.assertEqual(1, data)
    
if __name__ == '__main__':
    unittest.main()