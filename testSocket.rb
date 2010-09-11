require 'socket'
url = "www.dailymotion.com"
sock = TCPSocket.new(url, 80)
sock.sync = true
sock.puts "GET /en/ HTTP/1.0 \n\n"
while true
	line = sock.readline
	p line
end

