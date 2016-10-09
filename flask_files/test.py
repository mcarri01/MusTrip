import os
import mustrip
import unittest
import tempfile
from random import randint

class mustripTestCase(unittest.TestCase):

    def setUp(self):
        self.db_fd, mustrip.app.config['DATABASE'] = tempfile.mkstemp()
        mustrip.app.config['TESTING'] = True
        self.app = mustrip.app.test_client()

    def tearDown(self):
        os.close(self.db_fd)
        os.unlink(mustrip.app.config['DATABASE'])

    def test_post(self):
        for i in range(50):
            req = self.app.post('/getPlaylist', data=dict(
                lat=str(randint(0, 100)),
                lng=str(randint(0,100))
            ))
            self.assertEqual(req.status, '200 OK')

if __name__ == '__main__':
    unittest.main()