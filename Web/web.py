from flask import Flask, render_template, request, redirect
from sqlite_ import insert
from time import time
app = Flask(__name__)

class Bike:
        def __init__(self, *args):
                (self.id, self.vehicle_type, self.latitude, self.longitude, self.speed, self.direction, self.status, self.time) = args
        
        def replace(self, *args):
                (self.id, self.vehicle_type, self.latitude, self.longitude, self.speed, self.direction, self.status, self.time) = args
        
        def pprint(self):
                 print self.id, self.vehicle_type, self.latitude, self.longitude, self.speed, self.direction, self.status, self.time

        def csv(self):
                string = str(self.id) + ', ' + str(self.vehicle_type) + ', ' + str(self.latitude) + ', ' + str(self.longitude) + ', ' + str(self.speed) + ', ' + str(self.direction) + ', ' + str(self.status)
                return string 

bikes = []
def distance(lat1, long1, lat2, lon2):
		R = 6373.0
		
		lat1 = radians(lat1)
		lon1 = radians(lon1)
		lat2 = radians(lat2)
		lon2 = radians(lon2)
		
		dlon = lon2 - lon1
		dlat = lat2 - lat1
		
		a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
		c = 2 * atan2(sqrt(a), sqrt(1 - a))
		
		distance = R * c
		return distance
		
	
@app.route('/')
def index():
        for i in bikes:
                i.pprint()
        #title = "Temperature Control"
	#return render_template('index.html', title=title)
        return "success"

@app.route('/cats')
def cats():
	return render_template('cats.html')

@app.route('/upload/<id>/<vehicle_type>/<latitude>/<longitude>/<speed>/<direction>/<status>', methods = ['GET'])
def upload(id, vehicle_type, latitude, longitude, speed, direction, status):
        exists = False
        Tim = time()

        for i in bikes:
                if id == i.id:
                        exists = True
                        i.replace(id, vehicle_type, latitude, longitude, speed, direction, status, Tim)

        if not (exists):
                bikes.append(Bike(id, vehicle_type, latitude, longitude, speed, direction, status, Tim))
        
        return redirect('/')
	
@app.route('/retrieve/<latitude>/<longitude>', methods = ['GET'])
def retrieve(latitude, longitude):
        Tim = time()
        string = ''

        for i in bikes:
                #if ((time - i.time) < 15):
                string = string + i.csv() + '\n'

        return string

if __name__== '__main__':
	app.run('0.0.0.0', debug=True)
        
