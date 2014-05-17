#!/usr/bin/env python
import wx
import pytz
import dropbox, os.path, math, datetime, urllib2, subprocess
from dateutil import tz
from datetime import tzinfo
from dropbox.client import DropboxClient, DropboxOAuth2Flow, ErrorResponse
from dropbox.datastore import DatastoreManager, Date, DatastoreError

DROPBOX_APP_KEY = '8hz1oi5jqgnd8f2'
DROPBOX_APP_SECRET = 'ddjowt0v7nl1mun'




class GeoFrame(wx.Frame):
    def __init__(self, parent, id, title):
        wx.Frame.__init__(self, parent, id, title, size=(1200,600))#size=(600,500), style=wx.DEFAULT_DIALOG_STYLE

        hbox  = wx.BoxSizer(wx.HORIZONTAL)
        vbox1 = wx.BoxSizer(wx.VERTICAL)
        vbox2 = wx.BoxSizer(wx.VERTICAL)
        vbox3 = wx.GridSizer(2,2,0,0)
#         vbox4 = wx.GridBagSizer(0,0)
        vbox4 = wx.GridSizer(5,5,0,0)
        pnl1 = wx.Panel(self, -1, style=wx.SIMPLE_BORDER)
        pnl2 = wx.Panel(self, -1, style=wx.SIMPLE_BORDER)
        
        #Columns
        self.lc = wx.ListCtrl(self, -1, style=wx.LC_REPORT | wx.LC_SINGLE_SEL)
        self.lc.InsertColumn(0, 'File')
        self.lc.SetColumnWidth(0, 150)
        self.lc.InsertColumn(1, 'MTime')
        self.lc.SetColumnWidth(1, 250)
        self.lc.InsertColumn(2, 'Lat')
        self.lc.SetColumnWidth(2, 100)
        self.lc.InsertColumn(3, 'Long')
        self.lc.SetColumnWidth(3, 100)
        self.lc.InsertColumn(4, 'Dist')
        self.lc.SetColumnWidth(4, 250)
        
        vbox1.Add(pnl1, 1, wx.EXPAND | wx.ALL, 3)
        vbox1.Add(pnl2, 1, wx.EXPAND | wx.ALL, 3)
        vbox2.Add(self.lc, 1, wx.EXPAND | wx.ALL, 3)
        
        #Entry Boxes
        self.tc1 = wx.TextCtrl(pnl1, -1)
        self.tc2 = wx.TextCtrl(pnl1, -1)
        
        vbox3.AddMany([ (wx.StaticText(pnl1, -1, 'Latitude'),0, wx.ALIGN_CENTER),
                        (self.tc1, 0, wx.ALIGN_LEFT|wx.ALIGN_CENTER_VERTICAL),
                        (wx.StaticText(pnl1, -1, 'Longitude'),0, wx.ALIGN_CENTER_HORIZONTAL),
                        (self.tc2,0)])
        
        pnl1.SetSizer(vbox3)
        vbox4.Add(wx.Button(pnl2, 10, 'Set'), 0, wx.ALIGN_CENTER | wx.EXPAND,0)
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.Add(wx.Button(pnl2, 11, 'Copy'),0, wx.ALIGN_CENTER | wx.EXPAND,0)
        
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        
        vbox4.Add(wx.Button(pnl2, 12, 'No Loc.'), 0, wx.ALIGN_CENTER | wx.EXPAND,0)
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.Add(wx.Button(pnl2, 14, 'Search'), 0, wx.wx.ALIGN_CENTER | wx.EXPAND,0)
        
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        vbox4.AddStretchSpacer()
        
        vbox4.Add(wx.Button(pnl2, 13, 'Delete'), 0, wx.ALIGN_CENTER | wx.EXPAND,0)
        vbox4.AddStretchSpacer()
        vbox4.Add(wx.Button(pnl2, 16, 'Close'), 0, wx.ALIGN_CENTER | wx.EXPAND,0)
        vbox4.AddStretchSpacer()
        vbox4.Add(wx.Button(pnl2, 15, 'Refresh'), 0, wx.ALIGN_CENTER | wx.EXPAND,0)
        
        #bag
#         vbox4.Add(wx.Button(pnl2, 11, 'Copy'), (0,2),   (1,1), wx.ALIGN_RIGHT,0)
#         vbox4.AddStretchSpacer((1,2))
#         vbox4.Add(wx.Button(pnl2, 14, 'Search'), (2,2), (1,1), wx.ALIGN_RIGHT,0)
#         vbox4.AddStretchSpacer((3,2))
#         vbox4.Add(wx.Button(pnl2, 15, 'Refresh'), (4,2), (1,1), wx.ALIGN_RIGHT,0)
#         vbox4.Add(wx.Button(pnl2, 10, 'Set'), (0,0),   (1,1), wx.ALIGN_LEFT,0)
#         vbox4.AddStretchSpacer((1,0))
#         vbox4.Add(wx.Button(pnl2, 12, 'No Loc.'), (2,0), (1,1), wx.ALIGN_LEFT,0)
#         vbox4.AddStretchSpacer((3,0))
#         vbox4.Add(wx.Button(pnl2, 13, 'Delete'), (4,0), (1,1), wx.ALIGN_LEFT,0)
#         vbox4.AddStretchSpacer((1,1))
#         vbox4.AddStretchSpacer((3,1))
#         vbox4.Add(wx.Button(pnl2, 16, 'Close'), (4,1), (1,1), wx.ALIGN_BOTTOM,0)
        
        pnl2.SetSizer(vbox4)
        
        self.Bind (wx.EVT_BUTTON, self.OnSet, id=10)
        self.Bind (wx.EVT_BUTTON, self.OnCopy, id=11)
        self.Bind (wx.EVT_BUTTON, self.OnClear, id=12)
        self.Bind (wx.EVT_BUTTON, self.OnDelete, id=13)
        self.Bind (wx.EVT_BUTTON, self.OnSearch, id=14)
        self.Bind (wx.EVT_BUTTON, self.OnRefresh, id=15)
        self.Bind (wx.EVT_BUTTON, self.OnClose, id=16)
        
        hbox.Add(vbox1, 0, wx.EXPAND)
        hbox.Add(vbox2, 1, wx.EXPAND)
        self.SetSizer(hbox)
        
        #Load Initial Data
        self.refreshData()

    def OnSet(self, event):
        if not self.tc1.GetValue() or not self.tc2.GetValue():
            return
        try:
            float(self.tc1.GetValue())
            float(self.tc2.GetValue())
        except ValueError:
            print "Not a float"
            return
        
        sel_item = self.lc.GetFocusedItem()
        itemPath = self.lc.GetItemText(sel_item)
#         print str(itemPath)
        query = geoData.query(path=str(itemPath) )
#         print query
        date=Date()
        if len(query) > 0:
            gdata = query.pop() #get query response, should only be one...
            gdata.update(lat=float(self.tc1.GetValue()), long=float(self.tc2.GetValue()), lastLoc=date)
            self.lc.SetStringItem(sel_item, 2, self.tc1.GetValue())
            self.lc.SetStringItem(sel_item, 3, self.tc2.GetValue())
            self.lc.SetStringItem(sel_item, 4, "" )
#             self.lc.SetStringItem(sel_item, 4, str(date.to_datetime_local() ) )
            datastore.commit()
        else:#new item
            geoData.insert(path=itemPath, lat=float(self.tc1.GetValue()), long=float(self.tc2.GetValue()), lastLoc=date)
            self.lc.SetStringItem(sel_item, 2, self.tc1.GetValue())
            self.lc.SetStringItem(sel_item, 3, self.tc2.GetValue())
            self.lc.SetStringItem(sel_item, 4, "" )
#             self.lc.SetStringItem(sel_item, 4, str(date.to_datetime_local() ) )
            datastore.commit()
            
#         self.tc1.Clear()
#         self.tc2.Clear()

    def OnCopy(self, event):
#         print self.lc.GetFocusedItem()
        if self.lc.GetFocusedItem() == -1:
            return
        itemLat = self.lc.GetItemText(self.lc.GetFocusedItem(),2)
        itemLong = self.lc.GetItemText(self.lc.GetFocusedItem(),3)
        self.tc1.SetLabelText(itemLat)
        self.tc2.SetLabelText(itemLong)
        
    def OnClear(self, event):
        sel_item = self.lc.GetFocusedItem()
        itemPath = self.lc.GetItemText(sel_item)
#         print str(itemPath)
        query = geoData.query(path=str(itemPath) )
        date=Date()
        if len(query) > 0:
            gdata = query.pop()
            gdata.delete("lat")
            gdata.delete("long")
            gdata.update(lastLoc=date)
            self.lc.SetStringItem(sel_item, 2, "" )
            self.lc.SetStringItem(sel_item, 3, "" )
            self.lc.SetStringItem(sel_item, 4, "" )
#             self.lc.SetStringItem(sel_item, 4, str(date.to_datetime_local()) )
            datastore.commit()
            
    def OnDelete(self, event):
        sel_item = self.lc.GetFocusedItem()
        itemPath = self.lc.GetItemText(sel_item)
#         print str(itemPath)
        query = geoData.query(path=str(itemPath) )
        if len(query) > 0:
            gdata = query.pop()
            gdata.delete_record()
#             self.lc.DeleteItem(sel_item)
            self.lc.SetStringItem(sel_item, 2, "" )
            self.lc.SetStringItem(sel_item, 3, "" )
            self.lc.SetStringItem(sel_item, 4, "" )
            datastore.commit()
        
    def OnSearch(self,event):
        if not self.tc1.GetValue() or not self.tc2.GetValue():
            return
#         folder_metadata = client.metadata('/GeoDrive')
        for item in range(self.lc.GetItemCount()):
            itemPath = self.lc.GetItemText(item)
            query = geoData.query(path=str(itemPath) )
            if len(query) > 0:
                gdata = query.pop()
                if gdata.has("lat") and gdata.has("long"):
                    gdf = gdata.get_fields()
                    dist = distance_on_earth(float(self.tc1.GetValue()), float(self.tc2.GetValue()), gdf["lat"], gdf["long"])
#                     print str(item) + " " + str(dist)
                    bearing = GetBearing(float(self.tc1.GetValue()), float(self.tc2.GetValue()), gdf["lat"], gdf["long"])
                    self.lc.SetStringItem(item, 4, "%s\t%.3f miles" % (bearing, dist) ) #TODO 2 decimal pts, direction
                    

    def OnClose(self, event):
        self.Close()

    def OnRefresh(self, event):
        self.refreshData()
        

    def LookupHistory(self, geoFile):
        if (not os.path.isfile('cookies.txt')):
            return None, None
        dt = datetime.datetime.strptime(geoFile["client_mtime"], '%a, %d %B %Y %H:%M:%S +0000')
        unixtime = int((dt - datetime.datetime(1970, 1, 1)).total_seconds())
        url = "https://maps.google.com/locationhistory/b/0/kml?startTime=" + str(unixtime - 3600) + "000&endTime=" + str(unixtime) + "000"
        #TODO fix path...
        subprocess.call(["wget", "--load-cookies=cookies.txt", "-O", "location.tmp", url])
        f = open("location.tmp", "r")
        longitude=None
        latitude=None
        for line in f:
            if (line.rfind("<gx:coord>")>=0):
                end = line.rfind("</gx:coord>")
                longitude, latitude, alt =  line[10:end].split(" ")
            
        f.close()
        return latitude, longitude

    def refreshData(self):
        self.lc.DeleteAllItems()
        folder_metadata = client.metadata('/GeoDrive')
        for geoFile in folder_metadata["contents"]:
#             print geoFile["path"] + "\n\tclient_mtime:" + geoFile["client_mtime"] + "\n\tmodified:" + geoFile["modified"]
#             entry = geoData.query(path=geoFile["path"])
            num_items = self.lc.GetItemCount()
            self.lc.InsertStringItem(num_items, geoFile["path"])
            cmtime = datetime.datetime.strptime(geoFile["client_mtime"], '%a, %d %B %Y %H:%M:%S +0000')
            cmtime = cmtime.replace(tzinfo=pytz._UTC()).astimezone(tz.tzlocal())
            self.lc.SetStringItem(num_items, 1, str(cmtime) )
            
            query = geoData.query(path=geoFile["path"])
            if len(query) > 0:
                gdata = query.pop()
                gdf = gdata.get_fields() #get query response, should only be one...
#                 print gdata
#                 print geoFile["path"]
                dt = datetime.datetime.strptime(geoFile["client_mtime"], '%a, %d %B %Y %H:%M:%S +0000')
                unixMtime = int((dt - datetime.datetime(1970, 1, 1)).total_seconds())
#                 print unixMtime
                unixLtime = int((gdf["lastLoc"].to_datetime_utc()  - datetime.datetime(1970, 1, 1)).total_seconds())
#                 print unixLtime
                if unixMtime > unixLtime:
                    Nlat, Nlong = self.LookupHistory(geoFile)
                    if Nlat and Nlong:
                        self.lc.SetStringItem(num_items, 2, str(Nlat) )
                        self.lc.SetStringItem(num_items, 3, str(Nlong) )
                        date = Date()
#                         self.lc.SetStringItem(num_items, 4, str(date.to_datetime_local()) )
                        gdata.update(lat=float(Nlat), long=float(Nlong), lastLoc=date)
                        datastore.commit()
                else:
                    if gdata.has("lat"):
                        self.lc.SetStringItem(num_items, 2, str(gdf["lat"]) )
                    if gdata.has("long"):
                        self.lc.SetStringItem(num_items, 3, str(gdf["long"]) )
                    self.lc.SetStringItem(num_items, 4, "" )
#                     if gdata.has("lastLoc"):
    #                     self.lc.SetStringItem(num_items, 4, str(gdf["lastLoc"].to_datetime_local() ) )
            else:
#                 print geoFile["path"]
                Nlat, Nlong = self.LookupHistory(geoFile)
                if Nlat and Nlong:
                    self.lc.SetStringItem(num_items, 2, str(Nlat) )
                    self.lc.SetStringItem(num_items, 3, str(Nlong) )
                    date = Date()
                    geoData.insert(path=geoFile["path"], lat=float(Nlat), long=float(Nlong), lastLoc=date)
                    datastore.commit()
                


class MyApp(wx.App):
    def OnInit(self):
        gframe = GeoFrame(None, -1, 'GeoDrive Manager')
        gframe.Show()
        return True










def connect():
    if (not os.path.isfile('access_token.txt')):
        flow = dropbox.client.DropboxOAuth2FlowNoRedirect(DROPBOX_APP_KEY, DROPBOX_APP_SECRET)
        authorize_url = flow.start()
        print '1. Go to: ' + authorize_url
        print '2. Click "Allow" (you might have to log in first)'
        print '3. Copy the authorization code.'
        code = raw_input("Enter the authorization code here: ").strip()
        access_token, user_id = flow.finish(code)
        f = open('access_token.txt', 'w')
        f.write(access_token)
        f.close()
    else:
        f = open('access_token.txt', 'r')
        access_token = f.readline()
        f.close()
    return access_token

def GetBearing(center_y, center_x, y, x):
    if center_x == x and center_y == y:
        return ""
    angle = math.degrees(math.atan2(y - center_y, x - center_x))
    bearing1 = (angle + 360) % 360
    bearing2 = (90 - angle) % 360
    bearing = bearing2
#     print "gb: x=%2d y=%2d angle=%6.1f bearing1=%5.1f bearing2=%5.1f" % (x, y, angle, bearing1, bearing2)
    if bearing >= 348.75 or bearing <= 11.25:
        return "N"
    elif bearing >= 11.25 and bearing <= 33.75:
        return "NNE"
    elif bearing >= 33.75 and bearing <= 56.25:
        return "NE"
    elif bearing >= 56.25 and bearing <= 78.75:
        return "ENE"
    elif bearing >= 78.75 and bearing <= 101.25:
        return "E"
    elif bearing >= 101.25 and bearing <= 123.75:
        return "ESE"
    elif bearing >= 123.75 and bearing <= 146.25:
        return "SE"
    elif bearing >= 146.25 and bearing <= 168.75:
        return "SSE"
    elif bearing >= 168.75 and bearing <= 191.25:
        return "S"
    elif bearing >= 191.25 and bearing <= 213.75:
        return "SSW"
    elif bearing >= 213.75 and bearing <= 236.25:
        return "SW"
    elif bearing >= 236.25 and bearing <= 258.75:
        return "WSW"
    elif bearing >= 258.75 and bearing <= 281.25:
        return "W"
    elif bearing >= 281.25 and bearing <= 303.75:
        return "WNW"
    elif bearing >= 303.75 and bearing <= 326.25:
        return "NW"
    elif bearing >= 326.25 and bearing <= 348.75:
        return "NNW"

def distance_on_earth(lat1, long1, lat2, long2):
    # To get the distance in miles, multiply by 3960. To get the distance in kilometers, multiply by 6373.
    return distance_on_unit_sphere(lat1, long1, lat2, long2) * 3960
    
def distance_on_unit_sphere(lat1, long1, lat2, long2):
    # Convert latitude and longitude to spherical coordinates in radians.
    degrees_to_radians = math.pi/180.0
    # phi = 90 - latitude
    phi1 = (90.0 - lat1)*degrees_to_radians
    phi2 = (90.0 - lat2)*degrees_to_radians
    # theta = longitude
    theta1 = long1*degrees_to_radians
    theta2 = long2*degrees_to_radians
    cos = (math.sin(phi1)*math.sin(phi2)*math.cos(theta1 - theta2) + math.cos(phi1)*math.cos(phi2))
    arc = math.acos( cos )
    # Remember to multiply arc by the radius of the earth in your favorite set of units to get length.
    return arc

def main():
    global client
    global datastore
    global geoData
    access_token = connect()
    
    client = DropboxClient(access_token)

    print 'linked account: ', client.account_info()["display_name"]

    manager = DatastoreManager(client)
    datastore = manager.open_default_datastore()
#     manager.delete_datastore("default")
        
    geoData = datastore.get_table('GeoData')

    
    app = MyApp(0)
    app.MainLoop()


#     folder_metadata = client.metadata('/GeoDrive')
# #     print 'metadata: ', folder_metadata["contents"]
#     for geoFile in folder_metadata["contents"]:
# #         print file
#         print geoFile["path"] + "\n\tclient_mtime:" + geoFile["client_mtime"] + "\n\tmodified:" + geoFile["modified"]
#         entry = geoData.query(path=geoFile["path"])
#         print entry

#     print "Distance: " + str(distance_on_earth(33.644277, -117.842026, 33.645147, -117.831879))

#     geoData.insert(path='/GeoDrive/Work.txt', lat=33.644277, long=-117.842026, lastLoc=Date())
#     geoData.insert(path='/GeoDrive/Home.txt', lat=33.645147, long=-117.831879, lastLoc=Date())
    datastore.commit()


#TODO folder browser
#Search
#Multi Users

if __name__ == '__main__':
    main()