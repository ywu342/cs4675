#!/usr/bin/env python
# Copyright (c) Georgia Instituted of Technology
# Author: C. Chen, H. Tang
# Script functionality description
# Create time: 2016-04-17 17:04:50

import googlemaps
import MySQLdb
# from datetime import datetime
import requests
import pdb
import operator
import sys
import json
import numpy as np

# meters a mile has 
mile2m = 1609.34

# number of galons when tank is full
FULL = 20

# miles per gallon
MPG = 21.1

GOOGLE_KEY = 'AIzaSyDiIGEtWebcQxHSWIKzyS6J1H4HFfpomtY'
myGasFeedUrl = 'http://devapi.mygasfeed.com/'
myGasFeedKey = 'rfej9napna'

def get_station_price(lat,lng):
    price = None
    # query will be used to retrieve data from the database
    query = "SELECT price FROM GasPrice WHERE lat=%s and lng=%s"
    # open a database connection
    # potentially leakage of the password for the database, shall be encrypted and then stored and accept input from users
    connection = MySQLdb.connect (host = "localhost", user = "root", passwd = "", db = "RouteServiceDB")
    # prepare a cursor object using cursor() method
    cursor = connection.cursor ()
    # execute the SQL query using execute() method.
    cursor.execute (query, (float(lat), float(lng)))
    # fill the graph for this part of data
    for temp in cursor:
        price = temp[0]
    # close the cursor object
    cursor.close ()
    # close the connection
    connection.close ()
    return price

def update_station_price(lat,lng,price):
    # query will be used to retrieve data from the database
    query = "INSERT INTO GasPrice (price,lat,lng) VALUES (%s,%s,%s)"
    # open a database connection
    # potentially leakage of the password for the database, shall be encrypted and then stored and accept input from users
    connection = MySQLdb.connect (host = "localhost", user = "root", passwd = "", db = "RouteServiceDB")
    # prepare a cursor object using cursor() method
    cursor = connection.cursor ()
    # execute the SQL query using execute() method.
    cursor.execute (query, (float(price), float(lat), float(lng)))
    # close the cursor object
    cursor.close ()
    # close the connection
    connection.close ()

def deunicodify_hook(pairs):
    new_pairs = []
    for key, value in pairs: 
        if isinstance(value, unicode):
            value = value.encode('utf-8')
        if isinstance(key, unicode):
            key = key.encode('utf-8')
        new_pairs.append((key, value))
    return dict(new_pairs)

# urlGenerate: generate url to get price for
# a gas station.
# location is the gas station address with
# a dict {'lat': value, 'lng': value}
# fuel type could be: reg, mid, pre, diesel.
# distance is the radiu around the location
def url_generate(location, fuel_type='reg', distance=0.2):
    lat = location['lat']
    lon = location['lng']
    #pdb.set_trace()
    url = '/'.join([myGasFeedUrl, 'stations', 'radius', \
            str(lat), str(lon), str(distance), fuel_type, \
            'distance', myGasFeedKey+'.json'])
    return url

# get_price, retrieve the price for each gas station
# stations is a list of gas staions, each station is
# identified by its address represented as a
# dict {'lat':value, 'lng':value}
def get_price(stations):
    result=list()
    
    for station in stations:
        item = station.copy()
        price = get_station_price(station['lat'], station['lng']);
        if price is None:
            url = url_generate(station)
            response = requests.get(url)
            content = response.content.split('</pre></div></pre>')[-1]
            data = json.loads(content, object_pairs_hook=deunicodify_hook)
            try:
                data = json.loads(content, object_pairs_hook=deunicodify_hook)
            except:
                sys.exit('error with transferring data to json:\n %s' % content)
            status = data['status']['error']
            if status != 'NO':
                sys.exit('error with getting price data:\n %s' % content)
            gas_stations = data['stations']


            for tmp in gas_stations:
                #print tmp['id'], tmp['reg_price'], \
                #        tmp['lat'], tmp['lng'], tmp['address']
                if tmp['id'] in str(result):
                    continue
                item['id'] = tmp['id']
                try:
                    item['price'] = float(tmp['reg_price'])
                    update_station_price(station['lat'], station['lng'], item['price'], item['id'], item['key'])
                except:
                    gas_stations.remove(tmp)
                break
        else:
            item['price'] = price

        if 'price' in str(item):
            result.append(item.copy())

        response.close()
    return result

# filter, mainly sort the stations according to
# their distances from the start address
# start, string for address of start point
# dst, string for address of end point
# stations: list of gas stations, each contain lat and lon
# thresh: threshold to filter out the stations
def filter(start, dst, stations, thresh):
    #pdb.set_trace()
    result = list()
    thresh = thresh * mile2m

    gmaps = googlemaps.Client(key=GOOGLE_KEY)
    original = gmaps.distance_matrix(start, dst, mode='driving')
    if original['status'] != 'OK':
        sys.exit('Accessing to google map failed')
    od = original['rows'][0]['elements'][0]['distance']['value']
    status = original['rows'][0]['elements'][0]['status']
    if status != 'OK':
        sys.exit('No Route is Found')
    for station in stations:
        lat = station['lat']
        lon = station['lng']
        tmp = gmaps.distance_matrix(start, (lat, lon), mode='driving')
        status = tmp['rows'][0]['elements'][0]['status']
        if status != 'OK':
            sys.exit('No Route is Found')
        dist = tmp['rows'][0]['elements'][0]['distance']['value']
        station['key'] = dist/mile2m
        tmp = gmaps.distance_matrix((lat, lon), dst, mode='driving')
        status = tmp['rows'][0]['elements'][0]['status']
        if status != 'OK':
            sys.exit('No Route is Found')
        dist += tmp['rows'][0]['elements'][0]['distance']['value']
        if dist-od < thresh:
            result.append(station.copy())

    # sorting the stations
    result.sort(key=operator.itemgetter('key'))
    for i in range(len(result)-1):
        station = result[i]
        nxt_sat = result[i+1]
        lat = station['lat']
        lng = station['lng']
        nlat = nxt_sat['lat']
        nlng = nxt_sat['lng']

        tmp = gmaps.distance_matrix((nlat, nlng), (lat, lng), mode='driving')
        status = tmp['rows'][0]['elements'][0]['status']
        if status != 'OK':
            sys.exit('No Route is Found')
        dist = tmp['rows'][0]['elements'][0]['distance']['value']
        station['d'] = dist/mile2m
    
    station = result[-1]
    station['d'] = float(0)

    # print 'total stations:', len(result)
    #for station in result:
    #    print station
    return result



'''
advanced_n_stop routing calculate the cost using
dynamic programming 
stations: a list data struction contains infomation
about each gas station. It is sorted according
to the distance of each gas station to the source
address. Each gas station has the following infomation:
{'price': float, 'gap': float, 'extra': float, 'dist': float }
'price': the gas price for the gas station
'gap': the distance from current gas station to next gas station
'extra': extra miles introduced by selecting this gas station
'dist': distance to the final destination of the trip
i: index to the current gas station
g: rest gas in the tank
d: maximum number of stops during the trip
'''

def advanced_n_stop(stations, i, g, d, mpg):
    trace = list()
    value = float('inf')
    dist = stations[i]['d']  # distance to next gas station
    price = stations[i]['price']
   
    # out of gas before arrive this gas station
    if g < 0:
        return [trace, value]

    # at the last gas station, just fill the tank to full
    if i == len(stations)-1:
        value = (FULL-g) * price
        return [trace, value]

    # no more stops, just wait until tank is empty or, 
    # arrived the final destination
    if d == 0:
        [trace, value] = advanced_n_stop(stations, i+1, g-dist/mpg, 0, mpg)
        return [trace, value]

    # fill, then how much to fill ? emulate all filling that 
    # can get to a gas station
    lowbound = g * mpg
    upbound = FULL * mpg

    j = i
    distance = float('0')
    candidate = list()
    while j < len(stations)-1:
        distance += stations[j]['d']
        # the next station is out of scope
        if distance > upbound:
            break
        # locate stations that can arrive
        # after filling
        if distance > lowbound \
                and stations[j+1]['price'] < price:
            candidate.append(distance)
        j += 1
    
    # print candidate, lowbound, upbound
    llst = list()
    left = float('inf')
    for distance in candidate:
        [ltmp, rtmp] = advanced_n_stop(stations, i+1, (distance-dist)/mpg, d-1, mpg)
        rtmp += (distance/mpg - g) * price
        if rtmp < left:
            left = rtmp
            llst = ltmp
    
    [ltmp, rtmp] = advanced_n_stop(stations, i+1, FULL-dist/mpg, d-1, mpg)
    rtmp += (FULL - g) * price
    if rtmp < left:
        left = rtmp
        llst = ltmp
    
    llst.append(i+1)

    # not fill
    [rlst, right] = advanced_n_stop(stations, i+1, g-dist/mpg, d, mpg)
    if left < right:
        trace = llst
        value = left
    else:
        trace = rlst
        value = right
    return [trace, value]

'''
cost_simple: implements the simplest cost calculating function
             it calculate the cheapest cost assuming each time 
             fill the tank to full or not fill
'''
def n_stop(stations, i, g, d, mpg):
    trace = list()
    value = float('inf')

    # out of gas before arrive this gas station
    if g < 0:
        # print i, g, value
        return [trace, value] 

    dist = stations[i]['d']
    price = stations[i]['price']

    # at the last gas station, just fill the tank to full
    if i == len(stations)-1:
        value = (FULL-g) * price
        return [trace, value]
    
    # no more stops, just wait until tank is empty or,
    # arrived the final destination
    if d == 0:
        [trace, value] = n_stop(stations, i+1, g-dist/mpg, d, mpg)
        return [trace, value]
    
    # fill at current station
    [llst, left] = n_stop(stations, i+1, FULL-dist/mpg, d-1, mpg)
    left = left + price * (FULL-g)
    llst.append(i+1)
    # not fill at current station
    [rlst, right] = n_stop(stations, i+1, g-dist/mpg, d, mpg)

    if left < right:
        trace = llst
        value = left
    else:
        trace = rlst
        value = right

    return [trace, value]

'''
naive: implements the simplest cost calculating function
             it calculate the cheapest cost assuming each time 
             fill the tank to the level that can travel to
             the next station with cheapter price than current
             station if the left gas can not travel there, 
             there is no stop limitations 
'''
def naive(stations, i, g, mpg):
    trace = list()
    value = float('inf')

    # out of gas before arrive this gas station
    if g < 0:
        return [trace, value] 

    dist = stations[i]['d']
    price = stations[i]['price']

    # at the last gas station, just fill the tank to full
    if i == len(stations)-1:
        value = (FULL-g) * price
        return [trace, value]
    

    # get the nearest station that with cheaper price 
    # than current staion
    j = i+1
    distance = dist
    while j < len(stations) and stations[j]['price'] >= price:
        distance += stations[j]['d']
        j += 1
    
    if j >= len(stations):
        j = len(stations)-1
        distance -= stations[j]['d']

    # if distance to next cheapter station is out of 
    # scope of the gas in tank, need to fill with amount
    # to travel to the next, otherwise not
    r_gas = distance/mpg

    # current stations is the cheapest among the avaiable range
    # fill the tank to full
    if r_gas > FULL or price < stations[j]['price']:
        [lst, res] = naive(stations, i+1, FULL-dist/mpg, mpg)
        trace = lst
        trace.append(i+1)
        value = res + (FULL-g) * price

    # there is a cheapter one between the current station and final station
    # fill the tank to the level that can achive that station
    elif r_gas > g:
        [lst, res] = naive(stations, j, 0, mpg)
        trace = lst
        trace.append(i+1)
        value = res + (r_gas-g) * price
    # the left gas in the tank can get to cheaper gas station 
    # not fill
    else:
        [lst, res] = naive(stations, j, g-distance/mpg, mpg)
        trace = lst
        value = res

    return [trace, value]



'''
test program
'''
def main(fpath, start, dst, alg = 1):
    #start = '2232 Dunseath AVE NW, Atlanta, GA, 30318'
    #dst   = '5116 Highland Road, Baton Rouge, LA'
    #start = 'Atlanta, GA'
    #dst   = 'Marietta, GA'

    #pdb.set_trace()
    try:
        fh = open(fpath, 'r')
    except:
        sys.exit('open file error')
    data = fh.read()
    #pdb.set_trace()
    fh.close()
    stations = [dict(t) for t in set([tuple(d.items()) for d in
        json.loads(data, object_pairs_hook=deunicodify_hook)])]

    #pdb.set_trace()
    stations = filter(start, dst, stations, 0.6)
    #pdb.set_trace()
    stations = get_price(stations)
    #pdb.set_trace()
    #for i in range(len(stations)):
    #    print i+1, stations[i]
    #[lst1, cost1] = n_stop(stations, 0, 1.9, 4, MPG)
    if alg == 1:
        [lst, cost] = naive(stations, 0, 1.9, MPG)
    elif alg == 2:
        [lst, cost] = n_stop(stations, 0, 1.9, 4, MPG)
    elif alg == 3:
        [lst, cost] = advanced_n_stop(stations, 0, 1.9, 4, MPG)
    #[lst3, cost3] = advanced_n_stop(stations, 0, 1.9, 4, MPG)
    lst.reverse()
    result =[stations[i-1] for i in lst]
    #print lst
    print result
    '''
    print 'result of simple:'
    print lst1
    print cost1
    print 'result of cheapest:'
    print lst2
    print cost2
    print 'result of cost:'
    print lst3
    print cost3
    '''

if __name__ == '__main__':
    fpath = sys.argv[1]
    start = sys.argv[2]
    dest = sys.argv[3]
    alg = sys.argv[4]
    main(fpath, start, dest, int(alg))
