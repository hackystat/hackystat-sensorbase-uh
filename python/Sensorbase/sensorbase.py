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
import time
from datetime import datetime,timedelta

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

    def put_sensordata(self, datatype="", tool="", resource="", properties=""):
        """
        Used to put up new sensordata on the hackystat server. Creates and
        XML element tree according to the xml schema (hardcoded). Will probably
        need some refactoring later on.
        """

        time = self.get_timestamp()

        # build a tree structure
        e_sensordata = ET.Element("SensorData")

        e_timestamp = ET.SubElement(e_sensordata, "Timestamp")
        e_timestamp.text = time
        
        e_runtime = ET.SubElement(e_sensordata, "Runtime")
        e_runtime.text = time

        e_tool = ET.SubElement(e_sensordata, "Tool")
        e_tool.text = tool

        e_datatype = ET.SubElement(e_sensordata, "SensorDataType")
        e_datatype.text = datatype

        e_resource = ET.SubElement(e_sensordata, "Resource")
        e_resource.text = resource

        e_owner = ET.SubElement(e_sensordata, "Owner")
        e_owner.text = self.email

        e_properties = ET.SubElement(e_sensordata, "Properties")

        for property_key in properties.keys():
            e_property = ET.SubElement(e_properties, "Property")
            e_key = ET.SubElement(e_property, "Key")
            e_key.text = property_key
            e_value = ET.SubElement(e_property, "Value")
            e_value.text = properties[property_key]

        uri = "/sensordata/tryggvib@hi.is/"+time
        response = self.connection.request_put(uri, None,
                                               ET.tostring(e_sensordata))
        print response
        

    def get_timestamp(self,hour=0,minute=0):
        time_current = datetime.now()
        time_current = time_current + timedelta(hours=hour,minutes=minute)

        #Time format for both oldcurrent timestamp
        time_format = "%Y-%m-%dT%H:%M:%S.000"
        timestamp = time.strftime(time_format, time_current.timetuple())

        return timestamp

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
    properties= {'TotalLines':'137',
                 'LinesAdded':'10',
                 'LinesDeleted':'12',
                 'Repository':'svn://www.hackystat.org/',
                 'RevisionNumber':'2345',
                 'Author':'tryggvib@hi.is'}
    filename = 'file://home/tryggvib/Projects/Tests/file1'
    sensorbase = SensorBase(sys.argv[1], sys.argv[2], sys.argv[3])
    sensorbase.put_sensordata("Commit","Subversion",filename,properties)
