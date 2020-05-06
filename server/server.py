import socket
import sys
import struct
import win32api
import win32con

server = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind(("", 0))

print("Waiting for packets on {}:{}...".format(server.getsockname()[0], server.getsockname()[1]))

while (True):
	packet = server.recvfrom(128)
	data = packet[0]
	address = packet[1]
	if len(data) != 8:
		data = str(data, encoding="utf-8")
		if data == "LCLICK":
			win32api.mouse_event(win32con.MOUSEEVENTF_LEFTUP,win32api.GetCursorPos()[0],win32api.GetCursorPos()[1],0,0)
			print("Executing click at {}".format(win32api.GetCursorPos()))
		elif data == "LHOLD":
			print("Executing hold at {}".format(win32api.GetCursorPos()))
			win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN,win32api.GetCursorPos()[0],win32api.GetCursorPos()[1],0,0)
	else:
		xDelta, yDelta = tuple([-x for x in struct.unpack('>ff', data)])
		try:
			win32api.SetCursorPos((int(win32api.GetCursorPos()[0] + float(xDelta)), int(win32api.GetCursorPos()[1] + float(yDelta))))
		except:
			continue
		print("Moving cursor to: {}".format((int(win32api.GetCursorPos()[0] + float(xDelta)), int(win32api.GetCursorPos()[1] + float(yDelta)))))