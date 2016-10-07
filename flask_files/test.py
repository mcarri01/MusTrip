import os
import mustrip
import unittest
import tempfile

class mustripTestCase(unittest.TestCase):

    def setUp(self):
        self.db_fd, mustrip.app.config['DATABASE'] = tempfile.mkstemp()
        mustrip.app.config['TESTING'] = True
        self.app = mustrip.app.test_client()

    def tearDown(self):
        os.close(self.db_fd)
        os.unlink(mustrip.app.config['DATABASE'])

    def test_post(self):
        req = self.app.post('/getPlaylist', data=dict(
            lat='40',
            lng='-118'
        ))
        assert "dfsTrue"

if __name__ == '__main__':
    unittest.main()