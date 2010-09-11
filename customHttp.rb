require 'socket'
require 'logger'
require 'uri'

class CustomHttp

	@@separator = "\r\n"
	@@httpVersion = "HTTP/1.1"
	# HUGE_STRING_LENGTH
	@@bufferSize = 8192
	@@readTimeout = 15

	def initialize
		@logger = Logger.new($stderr)
		@logger.level = Logger::DEBUG

		@ips = {}
		
	end

	def get(url)
		uri = URI.parse(url)

		path = uri.path
		unless uri.query.nil?
			path << "?" + uri.query
		end
		# TODO: check if path is empty

		method = "HEAD"
		puts httpRequest(uri.host, uri.port, uri.path, method)
	end

	def httpRequest(host, port, path, method)
		request = getRequest(method, path, host)

		@logger.debug("Getting connection on #{host}:#{port}")
		socket = openSocket(host, port)
		@logger.debug("#{method} #{path}")
		socket.print(request)
		headers = readSocket(socket)
		@logger.debug("#{headers.size} characters were read.")
		socket.close

		headers = headers.split(@@separator)

		header = headers.shift
		httpVersion, statusCode, status = header.split
		
		#@logger.debug(statusLine)
		@logger.debug("#{statusCode} #{status}")

		if statusCode == "302"
			# TODO: scan Set-Cookie headers first
			headers.each { |header|
				#puts header
				if header =~ /^Location: /
					location = $'
					# TODO check if location is absolute
					# TODO set a limit in the recursion
					statusCode = httpRequest(host, port, location, method)
				end
			}
		end

		statusCode
	end

	private

	def readSocket(socket)
=begin
		# read blocking io
		# Should be wrapped in timeout(@@readTimeout) {}

		socket.sync = true
		# Buffered (sync = false) or not buffered (sync = true)

		# will wait till the client receives EOF, ie the server closes the connection
		content = socket.read

		# low level read, for buffered io
		#content = socket.sysread(@@bufferSize)

		# try a sysread first, then read if it failed ("sysread for buffered IO (IOError)")
		#content = socket.readpartial(@@bufferSize)
=end

		# read non blocking io
		content = nil
		# wait for the socket to become readable, but does not block
		if IO.select([socket], nil, nil, @@readTimeout)
			# set O_NONBLOCK as file descriptor then read
			content = socket.read_nonblock(@@bufferSize)
		end
		content
	end

	def getRequest(method, path, host)
		# TODO: add User-Agent
		# TODO: specify HTTP version

		[
			"#{method} #{path} #{@@httpVersion}",
			"Accept: */*",
			"Connection: close",
			"Host: #{host}",
			"",
			""
		].join(@@separator)

	end

	def openSocket(host, port)
		ip = @ips[host]
		if ip.nil?
			ip = IPSocket::getaddress(host)
			@ips[host] = ip
			@logger.debug("IP address for #{host}: " + ip)
		end

		socket = TCPSocket.open(ip, port)
		socket
	end

	# IPSocket::getaddress(host)
	def resolveIp(host)
		@logger.debug("DNS lookup of #{host}")

		# TODO: check if host is already an IP
		hostname, aliases, addressFamily, address = Socket::gethostbyname(host)
		# TODO: manage IPv6
		address.unpack('C*').join(".")
	end

end

http = CustomHttp.new
http.get("http://www.dailymotion.com/")
#http.get("http://www.truveo.com/")
#http.get("http://www.hotmail.com/")

