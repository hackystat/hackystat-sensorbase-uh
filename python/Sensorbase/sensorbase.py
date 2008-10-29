#!/usr/bin/python
# -*- coding: utf-8 -*-
"""
    sensorbase.py Communications with a Hackystat sensorbase via the REST API
    Copyright (C) 2008  Tryggvi Björgvinsson

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

from restful_lib import Connection

#These two shouldn't be needed in production environment.
import xml.etree.ElementTree as ET
import sys

class SensorBase:

    def __init__(self, host, email, password):
        """
        Set up a connection with the sensorbase host
        """
        
        self.host = host
        self.email = email
        self.password = password

        self.connection = Connection(self.host, self.email, self.password)

    def get_sensordata(self,user=None):
        """
        Deprecated! Written during development to get acquainted with
        Hackystat's REST API. Might become handy if sensorbase.py becomes
        a python lib to interact with hackystat. Refactoring needed?
        """
        
        if user == None:
            user = self.email

        response = self.connection.request_get("/sensordata"+user)
        xml_string = response[u'body']
        
        tree = ET.XML(xml_string)
        ET.dump(tree)

    def put_sensordata(self,properties=None):
        """
        Warning: Bad code!
        Used to put up new sensordata on the hackystat server. Just for
        testing purposes. Should not be used in production environment.
        If sensorbase.py becomes a python lib to interact with hackystat this
        method needs serious refactoring or more accurately reimplementation.
        """
        
        time = '2008-10-22T16:40:00.000'
        
        xml_string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SensorData><Timestamp>"+time+"</Timestamp><Runtime>"+time+"</Runtime><Tool>GNU Mailman</Tool><SensorDataType>Mail</SensorDataType><Resource>file://home/tryggvib/Projects/Tests/email</Resource><Owner>tryggvib@hi.is</Owner>"

        for property_key in properties.keys():
            xml_string = xml_string+"<Property><Key>"+property_key+\
                         "</Key><Value>"+properties[property_key]+\
                         "</Value></Property>"
            
        xml_string = xml_string + "</Properties></SensorData>"
        
        uri = "/sensordata/tryggvib@hi.is/"+time
        #response = self.connection.request_put(uri, None, xml_string)
        print xml_string
        #print response

    def get_sensor_datatype(self,data):
        """
        Deprecated! Written during development to get acquainted with
        Hackystat's REST API. Might become handy if sensorbase.py becomes
        a python lib to interact with hackystat. Refactoring needed?
        """
        
        name = data.attrib['Name']
        response = self.connection.request_get("/sensordatatypes/"+name)

        xml_string = response[u'body']

        tree = ET.XML(xml_string)
        return response[u'body']

    def get_projectdata(self,user=None,project=None,args=None):
        """
        Get project data from hackystat server. If no user is defined it
        uses the email login used to initiate the connection, if no project
        is defined it uses 'Default'. Arguments in a dictionary are just
        forwarded. Returns the body of the response. Would it be better for
        this function to be a generator of responses? Might be tricky to
        implement given the structure of hackystat repsonses.
        """

        if user == None:
            user = self.email

        if project == None:
            project = "Default"

        response = self.connection.request_get("/projects/"+\
                                               user+"/"+project+\
                                               "/sensordata",args)

        return response[u'body']

if __name__ == "__main__":
    #For testing purposes
    properties= {'TotalLines':'14',
                 'Citations':'2',
                 'Subject':'Testmail',
                 'Author':'tryggvib@hi.is'}
    sensorbase = SensorBase(sys.argv[1], sys.argv[2], sys.argv[3])
    sensorbase.put_sensordata(properties)

