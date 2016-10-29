import os
import json
import mustrip
import unittest
import tempfile
import string
import random
from random import randint

def generate_user():
        return ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for _ in range(10))

class mustripTestCase(unittest.TestCase):

    def setUp(self):
        self.db_fd, mustrip.app.config['DATABASE'] = tempfile.mkstemp()
        mustrip.app.config['TESTING'] = True
        self.app = mustrip.app.test_client()

    def tearDown(self):
        os.close(self.db_fd)
        os.unlink(mustrip.app.config['DATABASE'])
    
    def test_coords(self):
        for i in range(50):
            res = self.app.post('/getPlaylist', data=dict(
                lat=str(randint(0, 100)),
                lng=str(randint(0,100))
            ))
            self.assertEqual(res.status, '200 OK')

    def test_city(self):
        res = self.app.post('/playlistbycity', data=dict(
            city="Boston",
        ))
        data = json.loads(res.get_data(as_text=True))
        self.assertEqual(data['city'], "Boston")

    
    
    def test_new_user(self):
        user = generate_user()
        res = self.app.post("/addUser", data=dict(
            user= user,
        ))
        data = json.loads(res.get_data(as_text=True))
        self.assertEqual(data['status'], "success")
    
     
    def test_existing_user(self):
        res = self.app.post('/addUser', data=dict(
            user="Test",
        ))
        data = json.loads(res.get_data(as_text=True))
        self.assertEqual(data['status'], "User already exists")
        

    
    def test_add_playlist(self):
        res = self.app.post("/addTrack", data=dict(
            user="Test",
            trip_id="test",
            track="test2"
        ))
        data = json.loads(res.get_data(as_text=True))
        self.assertEqual(data['status'], "success")
    
    
    
    def test_get_playlists(self):
        res = self.app.post("/getPlaylists", data=dict(
            user="Test",
            trip_id="test"
        ))
        data = json.loads(res.get_data(as_text=True))
        self.assertEqual(True, isinstance(data, list))
    
    def test_add_trip(self):
        res = self.app.post("/addTrip", data=dict(
            user="Test",
            trip_id="test"
        ))
        data = json.loads(res.get_data(as_text=True))
        self.assertEqual(data['status'], "Trip already exists")
    
    def test_get_user(self):
        res = self.app.post("/getUser", data=dict(
            user="Test",
        ))
        data = json.loads(res.get_data(as_text=True))
        self.assertEqual(data['username'], "Test")

if __name__ == '__main__':
    unittest.main()