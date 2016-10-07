import ast
from pymongo import MongoClient
from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait
from geopy.geocoders import Nominatim

# Amount of cities to check
NUM_CITIES = 543
MONGODB_URI = "mongodb://mcarring:Keeker95@ds053156.mlab.com:53156/heroku_6132kr9d"

first_half = "https://cartocdn-ashbu.global.ssl.fastly.net/eliotvb/api/v1/map/eliotvb@32ec03a0@59d98d54ca3af37c9fed3fe8825b2740:0/1/attributes/"
second_half = "?callback=_cdbi_layer_attributes_890879453_2"
def init_driver():
        driver = webdriver.PhantomJS()
        driver.wait = WebDriverWait(driver, 5);
        return driver

def search(driver):

        client = MongoClient(MONGODB_URI)
        db = client.get_default_database()
        collection = db.cityData
        for i in range(1, NUM_CITIES):
                full_url = first_half + str(i) + second_half
                driver.get(full_url)
                body = driver.find_element_by_xpath("//html/body/pre")

                # Splice string to just get key value pairs
                city_index = body.text.index("city") - 2
                data = body.text[city_index:-2]

                # Convert to dict
                data_obj = ast.literal_eval(data)
                try:
                        geolocator = Nominatim()
                        location = geolocator.geocode(data_obj.get('city'))
                        lat = location.latitude
                        lng = location.longitude
                        data_obj['lat'] = lat
                        data_obj['lng'] = lng
                        collection.insert_one(data_obj)
                except GeocoderTimedOut as e:
                        print("Error: geocode time out")
                print(data_obj)

        driver.quit

if __name__ == "__main__":
        driver = init_driver()
        search(driver)
